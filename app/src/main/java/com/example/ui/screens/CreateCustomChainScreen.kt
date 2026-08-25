package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.domain.model.ChainDhikrItem
import com.example.ui.theme.ArabicSerifFamily
import com.example.viewmodel.CustomDhikrViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomChainScreen(
    viewModel: CustomDhikrViewModel,
    navController: NavController
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf<ChainDhikrItem>() }

    val catalogPhrases by viewModel.catalogTasbihPhrases.collectAsStateWithLifecycle()

    var showCatalogDialog by remember { mutableStateOf(false) }
    var showCustomPhraseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "إنشاء سلسلة أذكار",
                        fontWeight = FontWeight.Bold,
                        fontFamily = ArabicSerifFamily
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank() && items.isNotEmpty()) {
                                viewModel.saveChain(
                                    title = title.trim(),
                                    description = description.trim(),
                                    items = items.toList()
                                )
                                navController.popBackStack()
                            }
                        },
                        enabled = title.isNotBlank() && items.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = if (title.isNotBlank() && items.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم السلسلة (مثال: أذكار الصباح والتسبيح)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("الوصف (اختياري)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الأذكار والتسبيحات في السلسلة:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "الإجمالي: ${items.sumOf { it.targetCount }}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (items.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "لم تقم بإضافة أي أذكار بعد. اضغط على أحد الأزرار بالأسفل للإضافة.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
                    }
                }
            } else {
                itemsIndexed(items) { index, item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.arabicText,
                                    fontFamily = ArabicSerifFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Decrement
                                FilledTonalIconButton(
                                    onClick = {
                                        if (item.targetCount > 1) {
                                            items[index] = item.copy(targetCount = item.targetCount - 1)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Text(
                                    text = "${item.targetCount}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )

                                // Increment
                                FilledTonalIconButton(
                                    onClick = {
                                        items[index] = item.copy(targetCount = item.targetCount + 1)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                // Delete
                                IconButton(
                                    onClick = { items.removeAt(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showCatalogDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("من القائمة")
                    }

                    OutlinedButton(
                        onClick = { showCustomPhraseDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ذكر مخصص")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank() && items.isNotEmpty()) {
                            viewModel.saveChain(
                                title = title.trim(),
                                description = description.trim(),
                                items = items.toList()
                            )
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = title.isNotBlank() && items.isNotEmpty()
                ) {
                    Text("حفظ السلسلة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    // Catalog Dialog
    if (showCatalogDialog) {
        AlertDialog(
            onDismissRequest = { showCatalogDialog = false },
            title = { Text("اختر من التسبيحات الشائعة") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val defaultPhrases = if (catalogPhrases.isNotEmpty()) {
                        catalogPhrases.map { it.arabicText }
                    } else {
                        listOf(
                            "سُبْحَانَ اللَّهِ",
                            "الْحَمْدُ لِلَّهِ",
                            "لَا إِلَهَ إِلَّا اللَّهُ",
                            "اللَّهُ أَكْبَرُ",
                            "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
                            "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                            "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
                            "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ"
                        )
                    }

                    itemsIndexed(defaultPhrases) { idx, phrase ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    items.add(ChainDhikrItem(id = items.size + 1, arabicText = phrase, targetCount = 33))
                                    showCatalogDialog = false
                                }
                        ) {
                            Text(
                                text = phrase,
                                fontFamily = ArabicSerifFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCatalogDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Custom Phrase Dialog
    if (showCustomPhraseDialog) {
        var customText by remember { mutableStateOf("") }
        var customCountStr by remember { mutableStateOf("33") }

        AlertDialog(
            onDismissRequest = { showCustomPhraseDialog = false },
            title = { Text("إضافة ذكر مخصص") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it },
                        label = { Text("نص الذكر أو الدعاء") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customCountStr,
                        onValueChange = { customCountStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("العدد المستهدف (مثال: 33 أو 100)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val count = customCountStr.toIntOrNull() ?: 33
                        if (customText.isNotBlank() && count > 0) {
                            items.add(ChainDhikrItem(id = items.size + 1, arabicText = customText.trim(), targetCount = count))
                            showCustomPhraseDialog = false
                        }
                    },
                    enabled = customText.isNotBlank()
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomPhraseDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
