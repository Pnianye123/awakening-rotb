#!/usr/bin/env node
// 生成《方块之下》Modrinth 图标 PNG —— 无第三方依赖（只用 Node 内置 zlib）
// 视觉：黑底 + 淡金古城方块网格 → 双层金边 → 兜帽建造师剪影 + 两道金色眼缝 → 胸前金色 X 印记
//
// 用法：node tools/make-icon.js [输出路径] [尺寸]
//   node tools/make-icon.js                      → 默认路径 + 512x512
//   node tools/make-icon.js /tmp/i.png 128        → 指定路径和尺寸
//
// 2026-11-02（Efe）：原来尺寸写死 128x128，但 Modrinth 图标要求 512x512
//   → 改成参数化 + 最近邻放大（保持像素画风硬边，不糊）
'use strict';
const fs = require('fs');
const zlib = require('zlib');

const W = 128, H = 128;            // 绘制分辨率（像素画风在这上面最准）
const OUT = Math.max(1, parseInt(process.argv[3] || '512', 10) || 512);

const BG_DARK = [0x10, 0x10, 0x10];
const BG_MID  = [0x1a, 0x17, 0x12];
const GOLD    = [0xff, 0xd7, 0x00];
const GOLD_D  = [0xb8, 0x86, 0x0b];
const HOOD    = [0x1c, 0x1c, 0x20];
const HOOD_L  = [0x2a, 0x2a, 0x30];
const FACE    = [0x05, 0x05, 0x06];

const px = new Uint8Array(W * H * 4);

function idx(x, y) { return (y * W + x) * 4; }
function inb(x, y) { return x >= 0 && y >= 0 && x < W && y < H; }

function set(x, y, c, a = 255) {
  if (!inb(x, y)) return;
  const i = idx(x, y);
  px[i] = c[0]; px[i + 1] = c[1]; px[i + 2] = c[2]; px[i + 3] = a;
}

function blend(x, y, c, a) {
  if (!inb(x, y) || a <= 0) return;
  const i = idx(x, y);
  const sa = a / 255, da = px[i + 3] / 255;
  const oa = sa + da * (1 - sa);
  if (oa <= 0) return;
  px[i]     = Math.round((c[0] * sa + px[i]     * da * (1 - sa)) / oa);
  px[i + 1] = Math.round((c[1] * sa + px[i + 1] * da * (1 - sa)) / oa);
  px[i + 2] = Math.round((c[2] * sa + px[i + 2] * da * (1 - sa)) / oa);
  px[i + 3] = Math.round(oa * 255);
}

function rect(x0, y0, w, h, c, a = 255) {
  for (let y = y0; y < y0 + h; y++) for (let x = x0; x < x0 + w; x++) blend(x, y, c, a);
}

function strokeRect(x0, y0, w, h, c, t = 1, a = 255) {
  rect(x0, y0, w, t, c, a);
  rect(x0, y0 + h - t, w, t, c, a);
  rect(x0, y0, t, h, c, a);
  rect(x0 + w - t, y0, t, h, c, a);
}

function ellipse(cx, cy, rx, ry, c, a = 255) {
  for (let y = Math.floor(cy - ry); y <= Math.ceil(cy + ry); y++) {
    for (let x = Math.floor(cx - rx); x <= Math.ceil(cx + rx); x++) {
      const nx = (x - cx) / rx, ny = (y - cy) / ry;
      if (nx * nx + ny * ny <= 1) blend(x, y, c, a);
    }
  }
}

function line(x0, y0, x1, y1, t, c, a = 255) {
  const steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0)) * 2 + 1;
  for (let s = 0; s <= steps; s++) {
    const x = Math.round(x0 + (x1 - x0) * s / steps);
    const y = Math.round(y0 + (y1 - y0) * s / steps);
    for (let dy = 0; dy < t; dy++) for (let dx = 0; dx < t; dx++) blend(x + dx, y + dy, c, a);
  }
}

// ---------- 1. 背景渐变 ----------
for (let y = 0; y < H; y++) {
  const t = y / (H - 1);
  const c = [
    Math.round(BG_DARK[0] + (BG_MID[0] - BG_DARK[0]) * t),
    Math.round(BG_DARK[1] + (BG_MID[1] - BG_DARK[1]) * t),
    Math.round(BG_DARK[2] + (BG_MID[2] - BG_DARK[2]) * t),
  ];
  rect(0, y, W, 1, c);
}

// ---------- 2. 淡金古城方块网格 ----------
for (let gy = 0; gy < H; gy += 16) {
  for (let gx = 0; gx < W; gx += 16) {
    const edge = (gx % 32 === 0) || (gy % 32 === 0);
    const c = edge ? GOLD_D : GOLD;
    for (let t = 0; t < 16; t++) {
      blend(gx, gy + t, c, edge ? 26 : 16);
      blend(gx + t, gy, c, edge ? 26 : 16);
    }
  }
}

// ---------- 3. 双层金边 ----------
strokeRect(2, 2, W - 4, H - 4, GOLD, 2);
strokeRect(6, 6, W - 12, H - 12, GOLD_D, 1, 190);

// ---------- 4. 兜帽 + 阴影 ----------
ellipse(64, 60, 25, 28, HOOD);
ellipse(64, 61, 23, 26, HOOD_L);
ellipse(64, 63, 18, 21, FACE);

// ---------- 5. 金色眼缝 ----------
rect(49, 59, 12, 4, GOLD);
rect(67, 59, 12, 4, GOLD);
rect(50, 63, 10, 1, GOLD_D, 170);

// ---------- 6. 长袍主体（梯形） ----------
for (let y = 80; y <= 116; y++) {
  const t = (y - 80) / 36;
  const half = 20 + Math.round(t * 22);
  rect(64 - half, y, half * 2, 1, HOOD);
}
for (let y = 82; y <= 114; y++) {
  const t = (y - 82) / 32;
  const half = 20 + Math.round(t * 22);
  blend(64 - half, y, GOLD_D, 120);
  blend(64 + half - 1, y, GOLD_D, 120);
}

// ---------- 7. 胸前金色 X 印记 ----------
line(53, 88, 75, 108, 3, GOLD);
line(75, 88, 53, 108, 3, GOLD);
rect(56, 97, 16, 3, GOLD);

// ---------- 8. 底部金边高光 ----------
rect(52, 116, 24, 2, GOLD_D, 150);

// ---------- 编码 PNG ----------
// 最近邻放大到 OUT×OUT（Modrinth 要求 512×512）
const scale = OUT / W;
const outW = Math.round(W * scale);
const outH = Math.round(H * scale);
const outPx = new Uint8Array(outW * outH * 4);
for (let y = 0; y < outH; y++) {
  const sy = Math.min(H - 1, Math.floor(y / scale));
  for (let x = 0; x < outW; x++) {
    const sx = Math.min(W - 1, Math.floor(x / scale));
    const si = (sy * W + sx) * 4;
    const di = (y * outW + x) * 4;
    outPx[di] = px[si];
    outPx[di + 1] = px[si + 1];
    outPx[di + 2] = px[si + 2];
    outPx[di + 3] = px[si + 3];
  }
}

const raw = Buffer.alloc((outW * 4 + 1) * outH);
for (let y = 0; y < outH; y++) {
  raw[y * (outW * 4 + 1)] = 0; // filter: none
  Buffer.from(outPx.buffer, y * outW * 4, outW * 4).copy(raw, y * (outW * 4 + 1) + 1);
}

const CRC_TABLE = (() => {
  const t = new Int32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = (c & 1) ? (0xedb88320 ^ (c >>> 1)) : (c >>> 1);
    t[n] = c;
  }
  return t;
})();

function crc32(buf) {
  let c = 0xffffffff;
  for (let i = 0; i < buf.length; i++) c = CRC_TABLE[(c ^ buf[i]) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}

function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length, 0);
  const td = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(td), 0);
  return Buffer.concat([len, td, crc]);
}

const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(outW, 0);
ihdr.writeUInt32BE(outH, 4);
ihdr[8] = 8;   // bit depth
ihdr[9] = 6;   // color type RGBA
ihdr[10] = 0;  // compression
ihdr[11] = 0;  // filter
ihdr[12] = 0;  // interlace

const png = Buffer.concat([
  Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
  chunk('IHDR', ihdr),
  chunk('IDAT', zlib.deflateSync(raw, { level: 9 })),
  chunk('IEND', Buffer.alloc(0)),
]);

const out = process.argv[2] || '/home/yifan/awakening-rotb/icon.png';
fs.writeFileSync(out, png);
console.log('written', out, png.length, 'bytes', outW + 'x' + outH);
