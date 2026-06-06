function initDataFromBase64(base64String) {
    // web 端的 window.atob 可以解码 base64，再通过 decodeURIComponent 解决中文乱码
    const jsonString = decodeURIComponent(escape(window.atob(base64String)));
    initData(jsonString); // 调用原本的解析方法
}

/**
 * 供 Android 端调用的原生 JS 方法
 * @param jsonString 传递过来的日记段落 JSON 数组字符串
 */
function initData(jsonString) {
    const container = document.getElementById('diary-container');
    const data = JSON.parse(jsonString);

    let htmlContent = '';

    data.forEach(item => {
        if (item.type === 'date') {
            // 插入日期分隔符
            htmlContent += `<div class="date-divider"><span>${item.content}</span></div>`;
        } else if (item.type === 'text') {
            // 插入日记内容气泡
            htmlContent += `<div class="paragraph-bubble">${item.content}</div>`;
        }
    });

    container.innerHTML = htmlContent;

    // 【核心修复】不要马上回调！用 setTimeout 延迟 150 毫秒
    // 留给 WebView 足够的时间去把 HTML 渲染成可见的皮肤样式
    setTimeout(function() {
        if (window.AndroidShareBridge) {
            window.AndroidShareBridge.onRenderFinished();
        }
    }, 150); // 150 毫秒对用户完全无感知，但对多核 CPU 来说时间非常充裕
}