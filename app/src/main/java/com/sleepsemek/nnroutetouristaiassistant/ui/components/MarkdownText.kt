package com.sleepsemek.nnroutetouristaiassistant.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val paragraphs = remember(markdown) {
        markdown.trim().lines()
    }

    Column(
        modifier = modifier
    ) {
        paragraphs.forEach { rawLine ->
            val line = rawLine.trimEnd()

            when {
                line.matches(Regex("^#+\\s.*")) -> {
                    val text = line.replace(Regex("^#+\\s*"), "")
                    MarkdownStyledText(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                line.matches(Regex("^[-*]\\s+.*")) -> {
                    val itemText = line.replace(Regex("^[-*]\\s+"), "")
                    Row {
                        Text(
                            text = "✦ ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        MarkdownStyledText(
                            text = itemText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                line.isBlank() -> Spacer(modifier = Modifier.height(4.dp))

                else -> {
                    MarkdownStyledText(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun MarkdownStyledText(
    text: String,
    style: TextStyle,
    color: Color
) {
    val annotated = remember(text) {
        buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                when {
                    text.startsWith("**", i) -> {
                        val end = text.indexOf("**", i + 2)
                        if (end != -1) {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(text.substring(i + 2, end))
                            }
                            i = end + 2
                            continue
                        }
                    }

                    text.startsWith("*", i) -> {
                        val end = text.indexOf("*", i + 1)
                        if (end != -1) {
                            withStyle(
                                style = SpanStyle(
                                    fontStyle = FontStyle.Italic
                                )
                            ) {
                                append(text.substring(i + 1, end))
                            }
                            i = end + 1
                            continue
                        }
                    }
                }
                append(text[i])
                i++
            }
        }
    }

    Text(
        text = annotated,
        style = style.copy(color = color, lineHeight = style.lineHeight * 1.2f)
    )
}
