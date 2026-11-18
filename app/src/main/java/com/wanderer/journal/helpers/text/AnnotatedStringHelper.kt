package com.wanderer.journal.helpers.text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import com.wanderer.journal.auxiliary.interfaces.RichTextRule
import java.util.regex.Matcher

/**
 * 将原始文本按规则转换为 Compose 的 AnnotatedString。
 *
 * @param raw                原始文本
 * @param highlightedKeywords 高亮关键词列表（可为 null）
 * @param highlightColor     高亮颜色
 * @param rules              富文本规则
 * @param onLinkClick        点击富文本标签的回调（tagKey, tagData）
 */
fun hierarchicToAnnotatedString(
    raw: String,
    highlightedKeywords: List<String>?,
    highlightColor: Color,
    rules: List<RichTextRule>,
    onLinkClick: (tagKey: String, tagData: String) -> Unit,
): AnnotatedString {
    if (raw.isEmpty()) return AnnotatedString("")
    if (rules.isEmpty()) {
        return buildAnnotatedString {
            appendWithHighlight(raw, highlightedKeywords, highlightColor)
        }
    }

    return buildAnnotatedString {
        var cursor = 0

        while (cursor < raw.length) {
            // 1) 找出当前游标之后最先出现的匹配（与 Java 版一致的"最靠前优先"策略）
            var closestStart = -1
            var closestEnd = -1
            var closestRule: RichTextRule? = null
            var closestMatcher: Matcher? = null

            for (rule in rules) {
                val matcher = rule.pattern.matcher(raw)
                if (matcher.find(cursor)) {
                    if (closestStart == -1 || matcher.start() < closestStart) {
                        closestStart = matcher.start()
                        closestEnd = matcher.end()
                        closestRule = rule
                        closestMatcher = matcher
                    }
                }
            }

            // 2) 后面没有匹配了：剩余全部作为普通文本
            if (closestRule == null || closestMatcher == null) {
                appendWithHighlight(raw.substring(cursor), highlightedKeywords, highlightColor)
                break
            }

            // 3) 匹配项前面的普通文本
            if (closestStart > cursor) {
                appendWithHighlight(
                    raw.substring(cursor, closestStart),
                    highlightedKeywords,
                    highlightColor,
                )
            }

            // 4) 命中规则：生成富文本标签
            val displayText = closestRule.getDisplayText(closestMatcher)
            val tagData = closestRule.getTextTagData(closestMatcher)
            val tagKey = closestRule.getKey()

            // 用 LinkAnnotation 绑定点击，同时加 SpanStyle 做视觉样式
            withLink(
                LinkAnnotation.Clickable(
                    tag = "rich_$tagKey",
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = highlightColor,
                            textDecoration = TextDecoration.None    // 覆盖默认的下划线
                        )
                    ),
                    linkInteractionListener = {
                        closestRule.onClick(tagData)
                        onLinkClick(tagKey, tagData)
                    }
                )
            ) {
                withStyle(SpanStyle(color = highlightColor)) {
                    append(displayText)
                }
            }

            // 同时把数据以 StringAnnotation 存一份，方便日后解析保存时使用
            addStringAnnotation(
                tag = tagKey,
                annotation = tagData,
                start = length - displayText.length,
                end = length,
            )

            cursor = closestEnd
        }
    }
}

/**
 * 把一段普通文本追加到 AnnotatedString.Builder，并对其中命中的关键词做颜色高亮。
 */
private fun AnnotatedString.Builder.appendWithHighlight(
    text: String,
    keywords: List<String>?,
    color: Color,
) {
    if (text.isEmpty()) return
    if (keywords.isNullOrEmpty()) {
        append(text)
        return
    }

    // 用"区间合并"的方式：先算出所有命中区间，按起始排序后拼接，避免重叠区间造成 Span 覆盖
    val ranges = mutableListOf<IntRange>()
    val lowerText = text.lowercase()
    for (kw in keywords) {
        if (kw.isEmpty()) continue
        val lowerKw = kw.lowercase()
        var idx = lowerText.indexOf(lowerKw)
        while (idx >= 0) {
            ranges += idx until (idx + kw.length)
            idx = lowerText.indexOf(lowerKw, idx + kw.length)
        }
    }

    if (ranges.isEmpty()) {
        append(text)
        return
    }

    val merged = ranges.sortedBy { it.first }.fold(mutableListOf<IntRange>()) { acc, r ->
        val last = acc.lastOrNull()
        if (last != null && r.first <= last.last + 1) {
            acc[acc.lastIndex] = last.first..maxOf(last.last, r.last)
        } else {
            acc += r
        }
        acc
    }

    var pos = 0
    for (r in merged) {
        if (r.first > pos) append(text.substring(pos, r.first))
        withStyle(SpanStyle(color = color)) {
            append(text.substring(r.first, r.last + 1))
        }
        pos = r.last + 1
    }
    if (pos < text.length) append(text.substring(pos))
}