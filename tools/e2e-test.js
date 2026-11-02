// 《方块之下》端到端回归测试 —— 用 mineflayer 真实客户端连服务端跑剧情
// 用法：先启动服务端（./gradlew runServer），再 node tools/e2e-test.js
const path = require('path');
const mineflayer = require(path.join(__dirname, '..', 'node_modules', 'mineflayer'));
const { Vec3 } = require(path.join(__dirname, '..', 'node_modules', 'vec3'));

// ⚠️ 必须用固定名字「JimmyTest」—— run/ops.json 里已经有它的离线 UUID（level 4），
//    bot 才有 OP，才能用 /setblock、/item replace 这些服务端命令。
//    之前用唯一名 + RCON 授权，但 RCON 读响应是坏的 → bot 没 OP → 方块没放进去。
const BOT = 'JimmyTest';
const bot = mineflayer.createBot({
  host: '127.0.0.1', port: 25565, username: BOT,
  version: '1.21.4', auth: 'offline', hideErrors: true,
});
console.log('[BOT]', BOT);
const sleep = (ms) => new Promise(r => setTimeout(r, ms));
const sec = (n) => console.log('\n########## ' + n + ' ##########');
let failCount = 0;

function check(label, cond, detail) {
  console.log((cond ? '  ✅ ' : '  ❌ ') + label + (detail ? ' — ' + detail : ''));
  if (!cond) failCount++;
}

// ── debug 输出采集：每次请求独立一份，避免旧数据串味 ──
let debugBuf = '';
let debugWaiters = [];
let allMsgs = [];
let msgWaiters = [];
bot.on('message', (m) => {
  const t = m.toString();
  allMsgs.push(t);
  msgWaiters.forEach((w) => w(t));
  if (t.startsWith('觉醒者：') || t.startsWith('纪元：') || t.startsWith('背包：')
      || t.startsWith('切scene') || t.startsWith('共鸣方块：')) {
    debugBuf += t + '\n';
    console.log('[DEBUG]', t);
    debugWaiters.forEach((w) => w());
  }
});

/** 请求一次 /awakening debug，并等待它的输出到齐（判据：出现「背包：」）。 */
async function debug(timeoutMs = 12000) {
  debugBuf = '';
  bot.chat('/awakening debug');
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    if (debugBuf.includes('背包：')) {
      await sleep(200);
      return debugBuf;
    }
    await sleep(250);
  }
  return debugBuf;
}

/** 等待条件成立（轮询 debug 输出）。 */
async function waitDebug(pred, timeoutMs = 15000) {
  const deadline = Date.now() + timeoutMs;
  let buf = '';
  while (Date.now() < deadline) {
    buf = await debug(4000);
    if (buf && pred(buf)) return buf;
  }
  return buf;
}

bot.on('kicked', (r) => console.log('[KICKED]', r));
bot.on('error', (e) => console.log('[ERROR]', e.message));

let spawned = false;
bot.on('spawn', async () => {
  if (spawned) return;   // 死亡重生会再次触发 spawn，别重跑整个测试
  spawned = true;
  await sleep(4000);

  // 创造模式：防止被凋零之子/Boss 打死导致测试中断
  bot.chat('/gamemode creative');
  await sleep(1200);

  sec('0. 重置进度');
  bot.chat('/awakening reset');
  await sleep(1500);

  sec('1. /awakening start');
  bot.chat('/awakening start');
  let d = await waitDebug(b => b.includes('纪元：第一纪'));
  check('剧情已开始且在第一纪', d.includes('纪元：第一纪'), '');
  check('发放了长袍+记录仪+共鸣石',
    d.includes('awakening:builder_robe') && d.includes('awakening:recorder')
    && d.includes('awakening:resonance_stone'), '');

  sec('2. 第一纪 → 觉醒者');
  d = await debug();
  check('觉醒者已生成', d.includes('觉醒者：1'), d.match(/觉醒者：\d+/)?.[0] || '(无)');

  sec('3. skip 3 → 邪术师');
  bot.chat('/awakening skip 3');
  d = await waitDebug(b => b.includes('纪元：第三纪'));
  check('邪术师已生成', d.includes('邪术师：1'), d.match(/邪术师：\d+/)?.[0] || '(无)');

  sec('4. skip 7 → 凋零之子 + 坚守者');
  bot.chat('/awakening skip 7');
  d = await waitDebug(b => b.includes('纪元：第七纪'));
  check('凋零之子已生成', d.includes('凋零之子：1'), d.match(/凋零之子：\d+/)?.[0] || '(无)');
  check('坚守者已生成（lore：玩家用灵魂+尖啸体召唤）',
    d.includes('坚守者：1'), d.match(/坚守者：\d+/)?.[0] || '(无)');

  sec('5. skip 8 → 第八纪信标净化');
  bot.chat('/awakening skip 8');
  d = await waitDebug(b => b.includes('纪元：第八纪'));
  check('已到第八纪', d.includes('纪元：第八纪'), '');

  // ⚠️⚠️ 关键：第八纪 cutscene 有 12 行文本 × 22 tick ≈ 13.2 秒，
  //    播完（cutsceneDone=true）StoryManager.tick 才进入任务判定阶段。
  //    之前只等 2.5 秒就断言 → 必然误判成「净化失败」。
  d = await waitDebug(b => b.includes('切scene完成：true'), 40000);
  check('第八纪 cutscene 已播完', d.includes('切scene完成：true'),
    d.match(/切scene完成：\w+/)?.[0] || '(无)');

  sec('6. 净化流程：放共鸣方块 + 下界之星 → 使用 → 净化世界');
  // 用 bot 自己的游戏内命令放方块（bot 是 OP），走真实玩家路径，不依赖 RCON。
  // ⚠️ 坐标用 bot.entity.position（客户端与服务端一致，因为都在同一台机器的同一次会话）
  const bp = bot.entity.position.floored();
  const fx = bp.x + 2, fy = bp.y + 1, fz = bp.z;
  bot.chat(`/setblock ${fx} ${fy} ${fz} awakening:resonance_block`);
  await sleep(1500);
  // 独立验证：用 mod 自己的 debug 输出「共鸣方块：N」（不依赖命令回显）
  d = await debug();
  check('共鸣方块已放置', d.includes('共鸣方块：1'), d.match(/共鸣方块：\d+/)?.[0] || '(无)');

  // 下界之星由剧情在进入第八纪时发放（第八纪任务需要它）
  d = await waitDebug(b => b.includes('minecraft:nether_star'));
  check('下界之星已到手（第八纪发放）', d.includes('minecraft:nether_star'), '');

  // 强制把下界之星放进快捷栏 0 并选中 —— 保证「手持下界之星」这个前置条件成立
  bot.chat('/item replace entity @s hotbar.0 with minecraft:nether_star 1');
  await sleep(1500);
  bot.setQuickBarSlot(0);
  // 等服务端把快捷栏同步过来
  for (let i = 0; i < 20; i++) {
    if (bot.heldItem && bot.heldItem.name === 'nether_star') break;
    await sleep(300);
  }
  console.log('   [held]', bot.heldItem ? bot.heldItem.name : '(none)');

  // 触发净化：使用下界之星（UseItemCallback）
  // —— 玩家不需要精准瞄准方块，符合「碰一下」的直觉
  try {
    await bot.activateItem();
  } catch (e) {
    console.log('   [activateItem 异常]', e.message);
  }
  await sleep(3000);

  // 净化标志已设 → tick 判定任务完成 → 推进到第九纪
  d = await waitDebug(b => b.includes('纪元：第九纪'), 20000);
  check('净化完成（推进到第九纪）', d.includes('纪元：第九纪'), d.match(/纪元：.*/)?.[0] || '(无)');
  check('净化后凋零之子被清除', d.includes('凋零之子：0'), d.match(/凋零之子：\d+/)?.[0] || '(无)');

  sec('7. skip 9 → 第九纪邪术师×3');
  bot.chat('/awakening skip 9');
  d = await waitDebug(b => b.includes('纪元：第九纪'));
  check('邪术师×3 已生成', d.includes('邪术师：3'), d.match(/邪术师：\d+/)?.[0] || '(无)');

  sec('8. /awakening freeplay');
  bot.chat('/awakening freeplay');
  d = await waitDebug(b => b.includes('awakening:record_c'));
  check('自由段发放 10 件奖励',
    d.includes('awakening:record_c') && d.includes('awakening:awakened_token')
    && d.includes('awakening:soul_compass'), d.match(/背包：.*/)?.[0] || '(无)');

  console.log('\n========== 测试结束，失败项：' + failCount + ' ==========');
  bot.quit();
  setTimeout(() => process.exit(0), 1200);
});

setTimeout(() => { console.log('TIMEOUT'); process.exit(1); }, 150000);
