package com.example.resume

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 2.dp,
    cornerRadius: Dp = 8.dp,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        elevation = elevation,
        backgroundColor = Color.Transparent,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    title()
                    subtitle?.let { Column { Spacer(Modifier.height(4.dp)); it() } }
                }
                trailing?.let { it() }
            }

            Spacer(Modifier.height(6.dp))

            Column { content() }
        }
    }
}
