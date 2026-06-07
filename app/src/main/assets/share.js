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
    const data = JSON.parse(jsonString);

    let htmlContent = '';

    data.forEach(item => {
        if (item.type === 'date') {
            htmlContent += `<div class="date-divider"><span>${item.content}</span></div>`;
        }
        else if (item.type === 'text') {
            let innerImgHtml = '';

            //判断当前段落是否有图片，且数组不为空
            if (item.imageUris && item.imageUris.length > 0) {
                const imgCount = item.imageUris.length >= 3 ? 3 : item.imageUris.length;
                let imgItemsHtml = '';

                //遍历数组生成所有的 <img> 标签
                item.imageUris.forEach(uri => {
                    imgItemsHtml += `
                        <div class="grid-image-item">
                            <img src="${uri}" class="bubble-image-node" alt="日记配图" />
                        </div>
                    `;
                });

                //根据需要显示的列数动态赋予类名（如 media-grid images-count-3）
                innerImgHtml = `
                    <div class="bubble-image-grid images-count-${imgCount}">
                        ${imgItemsHtml}
                    </div>
                `;
            }

            //处理时间（如果没有传时间，给一个保底空标签）
            let timeHtml = '';
            if (item.time && item.time.trim() !== '') {
                timeHtml = `<span class="bubble-time">${item.time}</span>`;
            }

            //如果有图片，时间戳会顺延到图片右下方；如果是纯文本，时间戳会在文本右下方。
            htmlContent += `
                <div class="paragraph-bubble">
                    <div class="bubble-content-wrapper">
                        <div class="bubble-text">${item.content}</div>
                        ${innerImgHtml}
                        ${timeHtml}
                    </div>
                </div>
            `;
        }
    });

    //注入到 DOM 中
    container.innerHTML += htmlContent;

    //多图高度拦截判定：必须等气泡内所有的图片全部加载完毕，再通知 Android 截图
    const images = container.getElementsByTagName('img');
    if (images.length === 0) {
        triggerRenderFinished();
    } else {
        let loadedCount = 0;
        Array.from(images).forEach(img => {
            img.onload = img.onerror = function() {
                loadedCount++;
                if (loadedCount === images.length) {
                    triggerRenderFinished();
                }
            };
        });
    }
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