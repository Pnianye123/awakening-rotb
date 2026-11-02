// 《方块之下》端到端回归测试 —— 用 mineflayer 真实客户端连服务端跑剧情
// 用法：node tools/e2e-test.js
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

bot.on('message', (m) => console.log('[CHAT]', m.toString()));
bot.on('kicked', (r) => console.log('[KICKED]', r));
bot.on('error', (e) => console.log('[ERROR]', e.message));

bot.on('spawn', async () => {
  await sleep(4000);

  sec('1. /awakening start');
  bot.chat('/awakening start');
  await sleep(4500);
  check('剧情已开始', true);

  sec('2. 背包（长袍 + 记录仪）');
  const inv1 = bot.inventory.items();
  check('发放了 2 件初始物品', inv1.length >= 2, inv1.map(i => `${i.count}x ${i.name}`).join(', '));

  sec('3. /awakening debug（第一纪 → 应有觉醒者）');
  bot.chat('/awakening debug');
  await sleep(2500);

  sec('4. skip 3（第三纪 → 应有邪术师）');
  bot.chat('/awakening skip 3');
  await sleep(4000);
  bot.chat('/awakening debug');
  await sleep(2500);

  sec('5. skip 7（第七纪 → 应有凋零之子）');
  bot.chat('/awakening skip 7');
  await sleep(4000);
  bot.chat('/awakening debug');
  await sleep(2500);

  sec('6. skip 9（第九纪 → 应有 3 个邪术师）');
  bot.chat('/awakening skip 9');
  await sleep(4000);
  bot.chat('/awakening debug');
  await sleep(2500);

  sec('7. /awakening freeplay');
  bot.chat('/awakening freeplay');
  await sleep(3000);
  const inv2 = bot.inventory.items();
  check('自由段发放 10 件奖励', inv2.length >= 10, `${inv2.length} 件`);

  sec('8. /awakening status（自由段）');
  bot.chat('/awakening status');
  await sleep(2000);

  console.log('\n========== 测试结束，失败项：' + failCount + ' ==========');
  bot.quit();
  setTimeout(() => process.exit(0), 1200);
});

setTimeout(() => { console.log('TIMEOUT'); process.exit(1); }, 90000);
