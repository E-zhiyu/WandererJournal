function initDataFromBase64(base64String) {
    // web 端的 window.atob 可以解码 base64，再通过 decodeURIComponent 解决中文乱码
    const jsonString = decodeURIComponent(escape(window.atob(base64String)));
    addData(jsonString); // 调用原本的解析方法
}

/**
 * 供 Android 端调用的原生 JS 方法
 * @param jsonString 传递过来的日记段落 JSON 数组字符串
 */
function addData(jsonString) {
    const container = document.getElementById('diary-container');
    const bottomTextContainer = document.getElementById('bottom-text');

    // 1. 解析防御
    let data;
    try {
        data = JSON.parse(jsonString);
    } catch (e) {
        console.error('addData: invalid JSON', e);
        triggerRenderFinished();
        return;
    }
    if (!Array.isArray(data)) {
        console.error('addData: data is not an array');
        triggerRenderFinished();
        return;
    }

    // 2. 用临时容器构建，避免 innerHTML += 重建旧节点
    const fragment = document.createDocumentFragment();
    // 收集本次新增的图片，用于加载完成判定
    const newImages = [];
    // 收集 bottomText（保持原逻辑：最后一个生效）
    let bottomTextHtml = null;

    data.forEach(item => {
        if (!item || typeof item !== 'object') return;

        if (item.type === 'date') {
            const div = document.createElement('div');
            div.className = 'date-divider';
            const span = document.createElement('span');
            span.textContent = item.content ?? '';   // 文本节点，天然安全
            div.appendChild(span);
            fragment.appendChild(div);

        } else if (item.type === 'paragraph') {
            fragment.appendChild(buildParagraph(item, newImages));

        } else if (item.type === 'bottomText') {
            // 语义：data 允许为 HTML（页脚富文本），保留 innerHTML
            // 如果页脚也应是纯文本，把下面改成 bottomTextHtml = item.data ?? '' 并 textContent 赋值
            if (item.data !== undefined && item.data !== '') {
                bottomTextHtml = item.data;          // 注意：此处信任 HTML
            }
        }
    });

    // 3. 一次性追加，不重建已有节点
    container.appendChild(fragment);
    if (bottomTextHtml !== null && bottomTextContainer) {
        bottomTextContainer.innerHTML = bottomTextHtml;
    }

    // 4. 只等本次新增图片加载完成
    waitImagesSettled(newImages, triggerRenderFinished);
}

/**
 * 构建单个段落气泡
 * @param {object} item
 * @param {HTMLImageElement[]} newImages 输出参数：收集新建的 img 节点
 * @returns {HTMLElement}
 */
function buildParagraph(item, newImages) {
    const bubble = document.createElement('div');
    bubble.className = 'paragraph-bubble';

    const wrapper = document.createElement('div');
    wrapper.className = 'bubble-content-wrapper';

    // ---- 图片网格 ----
    if (Array.isArray(item.imageUris) && item.imageUris.length > 0) {
        const imgCount = Math.min(item.imageUris.length, 3);
        const grid = document.createElement('div');
        grid.className = `bubble-image-grid images-count-${imgCount}`;

        item.imageUris.forEach(uri => {
            const cell = document.createElement('div');
            cell.className = 'grid-image-item';

            const img = document.createElement('img');
            img.className = 'bubble-image-node';
            img.alt = '日记配图';
            img.decoding = 'async';
            // 用 DOM 属性赋值，避免属性逃逸；同时做协议白名单
            img.src = sanitizeUri(uri);
            // 坏图兜底：隐藏，避免占位破坏截图
            img.addEventListener('error', () => { img.style.visibility = 'hidden'; }, { once: true });

            cell.appendChild(img);
            grid.appendChild(cell);
            newImages.push(img);
        });

        wrapper.appendChild(grid);
    }

    // ---- 正文（纯文本，textContent 天然安全）----
    const text = document.createElement('div');
    text.className = 'bubble-text';
    text.textContent = item.content ?? '';
    wrapper.appendChild(text);

    // ---- 情绪标签 ----
    if (Array.isArray(item.emotionTags) && item.emotionTags.length > 0) {
        const emotions = document.createElement('div');
        emotions.className = 'bubble-emotions-container';
        item.emotionTags.forEach(tag => {
            const chip = document.createElement('span');
            chip.className = 'emotion-chip';
            chip.textContent = tag ?? '';
            emotions.appendChild(chip);
        });
        wrapper.appendChild(emotions);
    }

    // ---- 位置 + 时间 ----
    const locationStr = typeof item.location === 'string' ? item.location.trim() : '';
    const timeStr = typeof item.time === 'string' ? item.time.trim() : '';
    if (locationStr || timeStr) {
        const meta = document.createElement('div');
        meta.className = 'bubble-meta';
        if (locationStr) {
            const loc = document.createElement('span');
            loc.className = 'bubble-location';
            loc.textContent = locationStr;
            meta.appendChild(loc);
        }
        if (timeStr) {
            const t = document.createElement('span');
            t.className = 'bubble-time';
            t.textContent = timeStr;
            meta.appendChild(t);
        }
        wrapper.appendChild(meta);
    }

    bubble.appendChild(wrapper);
    return bubble;
}

/**
 * URI 白名单，防止 javascript: / vbscript: 等伪协议
 * 允许：http、https、data:image、file、content、blob、相对路径
 */
function sanitizeUri(uri) {
    const s = String(uri ?? '').trim();
    if (!s) return '';
    // 相对路径 / 无协议：直接放行
    if (!/^[a-zA-Z][a-zA-Z0-9+.-]*:/.test(s)) return s;
    const lower = s.toLowerCase();
    if (lower.startsWith('http://') || lower.startsWith('https://') ||
        lower.startsWith('data:image/') || lower.startsWith('file://') ||
        lower.startsWith('content://') || lower.startsWith('blob:')) {
        return s;
    }
    console.warn('sanitizeUri: blocked uri', s);
    return '';
}

/**
 * 等待一组图片全部 load/error（已完成的立即计入）
 */
function waitImagesSettled(imgs, callback) {
    const pending = imgs.filter(img => !img.complete);
    if (pending.length === 0) {
        // 仍然异步回调，保持与 onload 路径一致的行为
        setTimeout(callback, 0);
        return;
    }
    let left = pending.length;
    const done = () => { if (--left === 0) callback(); };
    pending.forEach(img => {
        img.addEventListener('load', done, { once: true });
        img.addEventListener('error', done, { once: true });
    });
}

/**
 * 通知 Java 端网页已加载完毕
 */
function triggerRenderFinished() {
    setTimeout(function() {
        if (window.AndroidShareBridge) {
            window.AndroidShareBridge.onRenderFinished();
        }
    }, 150);
}