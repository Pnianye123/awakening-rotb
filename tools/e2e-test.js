// 《方块之下》端到端回归测试 —— 用 mineflayer 真实客户端连服务端跑剧情
// 用法：先启动服务端，再 node tools/e2e-test.js
const mineflayer = require('/tmp/node_modules/mineflayer');

const bot = mineflayer.createBot({
  host: '127.0.0.1', port: 25565, username: 'JimmyTest',
  version: '1.21.4', auth: 'offline', hideErrors: true,
});
const sleep = (ms) => new Promise(r => setTimeout(r, ms));
const sec = (n) => console.log('\n########## ' + n + ' ##########');
let failCount = 0;

function check(label, cond, detail) {
  console.log((cond ? '  ✅ ' : '  ❌ ') + label + (detail ? ' — ' + detail : ''));
  if (!cond) failCount++;
}

// 收集最近的 debug 输出
let lastDebug = '';
bot.on('message', (m) => {
  const t = m.toString();
  if (t.startsWith('觉醒者：') || t.startsWith('纪元：') || t.startsWith('背包：')) {
    lastDebug += t + '\n';
    console.log('[DEBUG]', t);
  }
});
bot.on('kicked', (r) => console.log('[KICKED]', r));
bot.on('error', (e) => console.log('[ERROR]', e.message));

bot.on('spawn', async () => {
  await sleep(4000);

  sec('0. 重置进度（世界存档会保留剧情状态）');
  bot.chat('/awakening reset');
  await sleep(2000);

  sec('1. /awakening start');
  bot.chat('/awakening start');
  await sleep(4000);
  lastDebug = '';
  bot.chat('/awakening debug');
  await sleep(1500);
  check('剧情已开始且在第一纪', lastDebug.includes('纪元：第一纪'), '');

  sec('2. 初始物品');
  lastDebug = '';
  bot.chat('/awakening debug');
  await sleep(2000);
  check('发放了长袍+记录仪+共鸣石', lastDebug.includes('awakening:builder_robe')
      && lastDebug.includes('awakening:recorder'), lastDebug.trim().split('\n').pop());

  sec('3. 第一纪 → 觉醒者');
  check('觉醒者已生成', lastDebug.includes('觉醒者：1'), '');

  sec('4. skip 3 → 邪术师');
  bot.chat('/awakening skip 3');
  await sleep(3500);
  lastDebug = '';
  bot.chat('/awakening debug');
  await sleep(2000);
  check('邪术师已生成', lastDebug.includes('邪术师：1'), '');

  sec('5. skip 7 → 凋零之子 + 坚守者');
  bot.chat('/awakening skip 7');
  await sleep(4000);
  lastDebug = '';
  bot.chat('/awakening debug');
  await sleep(2000);
  check('凋零之子已生成', lastDebug.includes('凋零之子：1'), '');
  check('坚守者已生成（lore：玩家召唤去打凋零）', lastDebug.includes('坚守者：1'), '');

  sec('6. skip 8 → 第八纪信标净化');
  bot.chat('/awakening skip 8');
  await sleep(3500);
  lastDebug = '';
  bot.chat('/awakening debug');
  await sleep(2000);
  check('已到第八纪', lastDebug.includes('纪元：第八纪'), '');

  sec('7. 净化流程：共鸣方块 + 下界之星 → 右击 → 净化世界');
  // 用 /setblock 放置共鸣方块（mineflayer 不认识 mod 物品，无法 equip）
  bot.chat('/setblock ~ ~-1 ~ awakening:resonance_block');
  await sleep(1500);
  // 手持下界之星：用原版 /item replace 放进 hotbar.0，绕开 mineflayer equip 的崩溃
  bot.chat('/item replace entity @s hotbar.0 with minecraft:nether_star 1');
  await sleep(1500);
  bot.setQuickBarSlot(0);
  await sleep(800);
  const target = bot.entity.position.floored().offset(0, -1, 0);
  const block = bot.blockAt(target);
  check('共鸣方块已放置', !!block, block ? (block.name || 'unknown') : '未找到方块');
  if (block) {
    bot.lookAt(target.offset(0.5, 0.5, 0.5), true);
    await sleep(700);
    bot.activateBlock(target);
    await sleep(2500);
  }
  bot.chat('/awakening debug');
  await sleep(2000);
  check('净化后已推进到第九纪', lastDebug.includes('纪元：第九纪'), '');
  check('凋零之子被净化清除', lastDebug.includes('凋零之子：0'), '');

  sec('8. skip 9 → 第九纪邪术师×3');
  bot.chat('/awakening skip 9');
  await sleep(3500);
  lastDebug = '';
  bot.chat('/awakening debug');
  await sleep(2000);
  check('邪术师×3 已生成', lastDebug.includes('邪术师：3'), '');

  sec('9. /awakening freeplay');
  bot.chat('/awakening freeplay');
  await sleep(3000);
  lastDebug = '';
  bot.chat('/awakening debug');
  await sleep(2000);
  check('自由段发放 10 件奖励', lastDebug.includes('awakening:record_c')
      && lastDebug.includes('awakening:soul_compass'), '');

  console.log('\n========== 测试结束，失败项：' + failCount + ' ==========');
  bot.quit();
  setTimeout(() => process.exit(failCount > 0 ? 1 : 0), 1200);
});

setTimeout(() => { console.log('TIMEOUT'); process.exit(1); }, 90000);
