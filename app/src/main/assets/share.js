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
            htmlContent += `<div class="date-divider"><span>${item.content}</span></div>`;
        }
        else if (item.type === 'text') {
            let innerImgHtml = '';

            // 判断当前段落是否有图片，且数组不为空
            if (item.imageUris && item.imageUris.length > 0) {
                const imgCount = item.imageUris.length >= 3 ? 3 : item.imageUris.length;
                let imgItemsHtml = '';

                // 遍历数组生成所有的 <img> 标签
                item.imageUris.forEach(uri => {
                    imgItemsHtml += `
                        <div class="grid-image-item">
                            <img src="${uri}" class="bubble-image-node" alt="日记配图" />
                        </div>
                    `;
                });

                // 根据图片数量动态赋予类名（如 media-grid images-count-3）
                innerImgHtml = `
                    <div class="bubble-image-grid images-count-${imgCount}">
                        ${imgItemsHtml}
                    </div>
                `;
            }

            // 图文完美融合在同一个气泡中
            htmlContent += `
                <div class="paragraph-bubble">
                    <div class="bubble-text">${item.content}</div>
                    ${innerImgHtml}
                </div>
            `;
        }
    });

    container.innerHTML = htmlContent;

    // 多图高度拦截判定：必须等气泡内所有的图片全部加载完毕，再通知 Android 截图
    const images = container.getElementsByTagName('img');
    if (images.length === 0) {
        triggerRenderFinished();
    } else {
        let loadedCount = 0;
        Array.from(images).forEach(img => {
            img.onload = img.onerror = function() {
                loadedCount++;
                //TODO:每加载一个就通知Java的进度条对话框
                if (loadedCount === images.length && window.AndroidShareBridge) {
                    window.AndroidShareBridge.onRenderFinished();
                }
            };
        });
    }
}