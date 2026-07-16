package com.budgettracker.core.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgettracker.core.model.Category

data class CategoryUiModel(
    val id: String,
    val name: String,
    val description: String,
    val icon: MaterialIconOption,
    val color: String = "#64748B",
)

data class MaterialIconOption(
    val name: String,
    val label: String,
    val imageVector: ImageVector,
)

data class MaterialIconSection(
    val title: String,
    val icons: List<MaterialIconOption>,
)

fun Category.asCategoryUiModel(): CategoryUiModel =
    CategoryUiModel(
        id = id,
        name = name,
        description = defaultNameKey ?: "Custom category",
        icon = MaterialIconOptions.firstOrNull { it.name == icon }
            ?: MaterialIconOptions.first { it.name == "category" },
        color = color,
    )

fun String.asCategoryIcon(): ImageVector =
    MaterialIconOptions.firstOrNull { it.name == this }?.imageVector ?: Icons.Filled.Category

fun defaultCategoryUiModels(): List<CategoryUiModel> = listOf(
    CategoryUiModel(
        id = "petrol",
        name = "Petrol",
        description = "Fuel and vehicle cost",
        icon = MaterialIconOptions.first { it.name == "local_gas_station" },
        color = "#EAB308",
    ),
    CategoryUiModel(
        id = "food",
        name = "Food",
        description = "Meals, drinks, groceries",
        icon = MaterialIconOptions.first { it.name == "restaurant" },
        color = "#EF4444",
    ),
    CategoryUiModel(
        id = "online_shopping",
        name = "Online Shopping",
        description = "Shopping and parcels",
        icon = MaterialIconOptions.first { it.name == "shopping_bag" },
        color = "#EC4899",
    ),
    CategoryUiModel(
        id = "transport",
        name = "Transport",
        description = "Ride, toll, parking",
        icon = MaterialIconOptions.first { it.name == "directions_car" },
        color = "#F97316",
    ),
    CategoryUiModel(
        id = "rental_fee",
        name = "Rental Fee",
        description = "House and fixed living cost",
        icon = MaterialIconOptions.first { it.name == "home" },
        color = "#3B82F6",
    ),
    CategoryUiModel(
        id = "doctor",
        name = "Doctor",
        description = "Health and clinic",
        icon = MaterialIconOptions.first { it.name == "medical_services" },
        color = "#06B6D4",
    ),
)

fun List<CategoryUiModel>.withEditableCategory(categoryName: String?): List<CategoryUiModel> {
    val normalizedCategoryName = categoryName?.trim().orEmpty()
    if (normalizedCategoryName.isBlank()) return this
    if (any { it.name.equals(normalizedCategoryName, ignoreCase = true) }) return this

    return this + CategoryUiModel(
        id = "editing_${normalizedCategoryName.lowercase().replace(' ', '_')}",
        name = normalizedCategoryName,
        description = "Current category",
        icon = MaterialIconOptions.first { it.name == "category" },
        color = "#64748B",
    )
}

@Composable
fun CategoryManagerScreen(
    categories: List<CategoryUiModel>,
    selectedCategoryId: String?,
    onBack: () -> Unit,
    onCategoryClick: (CategoryUiModel) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Category",
    helperText: String = "Tap to select",
    onAddCategoryClick: (() -> Unit)? = null,
    onCategoryEditClick: ((CategoryUiModel) -> Unit)? = null,
    topContent: (@Composable () -> Unit)? = null,
) {
    var editMode by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(top = 18.dp, bottom = 18.dp),
        ) {
            CategoryManagerHeader(
                title = title,
                helperText = helperText,
                onBack = onBack,
                editMode = editMode,
                onEditClick = onCategoryEditClick?.let {
                    { editMode = !editMode }
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            topContent?.let {
                it()
                Spacer(modifier = Modifier.height(14.dp))
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(categories, key = { it.id }) { category ->
                    CategoryManagerRow(
                        category = category,
                        selected = category.id == selectedCategoryId,
                        onClick = { onCategoryClick(category) },
                        onEditClick = if (editMode) onCategoryEditClick?.let { editClick ->
                            { editClick(category) }
                        } else {
                            null
                        },
                    )
                }
            }
        }

        if (onAddCategoryClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Accent)
                    .clickable(onClick = onAddCategoryClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add category",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun CategoryManagerHeader(
    title: String,
    helperText: String,
    onBack: () -> Unit,
    editMode: Boolean,
    onEditClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        RoundIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            onClick = onBack,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = helperText,
                color = MutedText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Black,
            )
        }
        RoundIconButton(
            icon = Icons.Filled.Edit,
            contentDescription = if (editMode) "Done editing categories" else "Edit categories",
            onClick = onEditClick ?: {},
            enabled = onEditClick != null,
            active = editMode,
        )
    }
}

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    active: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, if (active) AccentLight else Stroke, RoundedCornerShape(18.dp))
            .background(if (active) SelectedPanel else ButtonPanel.copy(alpha = 0.78f))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White.copy(alpha = if (enabled) 1f else 0.35f),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun CategoryManagerRow(
    category: CategoryUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = if (selected) AccentLight else Stroke,
                shape = RoundedCornerShape(18.dp),
            )
            .background(if (selected) SelectedPanel else Panel)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(category.color.toCategoryColor()),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = category.icon.imageVector,
                contentDescription = category.name,
                tint = Color.White,
                modifier = Modifier.size(23.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = category.name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        when {
            onEditClick != null -> {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onEditClick)
                        .padding(5.dp),
                )
            }

            selected -> {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }

            else -> {
                Spacer(modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun CategoryEditDialog(
    title: String,
    name: String,
    color: String,
    selectedIconName: String,
    iconSections: List<MaterialIconSection>,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    archiveButton: (@Composable (() -> Unit))? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Category name") },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Color",
                    color = MutedText,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(10.dp))
                CategoryColorPicker(
                    selectedColor = color.normalizedCategoryColor(),
                    onColorSelected = onColorChange,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Material icon",
                    color = MutedText,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(10.dp))
                IconPicker(
                    iconSections = iconSections,
                    selectedIconName = selectedIconName,
                    onIconSelected = onIconSelected,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            if (archiveButton == null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            } else {
                Row {
                    archiveButton()
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        },
    )
}

@Composable
private fun CategoryColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CategoryColorOptions.chunked(6).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowColors.forEach { colorHex ->
                    val selected = selectedColor.equals(colorHex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) Color.White else Stroke,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .background(colorHex.toCategoryColor())
                            .clickable { onColorSelected(colorHex) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected color",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
                repeat(6 - rowColors.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun IconPicker(
    iconSections: List<MaterialIconSection>,
    selectedIconName: String,
    onIconSelected: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.heightIn(max = 360.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(iconSections, key = { it.title }) { section ->
            Text(
                text = section.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(modifier = Modifier.height(8.dp))
            IconSectionGrid(
                iconOptions = section.icons,
                selectedIconName = selectedIconName,
                onIconSelected = onIconSelected,
            )
        }
    }
}

@Composable
private fun IconSectionGrid(
    iconOptions: List<MaterialIconOption>,
    selectedIconName: String,
    onIconSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        iconOptions.chunked(4).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowOptions.forEach { option ->
                    val selected = option.name == selectedIconName
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 1.dp,
                                color = if (selected) AccentLight else Stroke,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .background(if (selected) SelectedPanel else ButtonPanel)
                            .clickable { onIconSelected(option.name) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = option.imageVector,
                            contentDescription = option.label,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                repeat(4 - rowOptions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

val MaterialIconSections = listOf(
    MaterialIconSection(
        title = "Entertainment",
        icons = listOf(
            MaterialIconOption("movie", "Movie", Icons.Filled.Movie),
            MaterialIconOption("theaters", "Theaters", Icons.Filled.Theaters),
            MaterialIconOption("music_note", "Music", Icons.Filled.MusicNote),
            MaterialIconOption("sports_esports", "Games", Icons.Filled.SportsEsports),
        ),
    ),
    MaterialIconSection(
        title = "Food",
        icons = listOf(
            MaterialIconOption("restaurant", "Restaurant", Icons.Filled.Restaurant),
            MaterialIconOption("fastfood", "Fast food", Icons.Filled.Fastfood),
            MaterialIconOption("local_cafe", "Cafe", Icons.Filled.LocalCafe),
            MaterialIconOption("local_pizza", "Pizza", Icons.Filled.LocalPizza),
            MaterialIconOption("bakery_dining", "Bakery", Icons.Filled.BakeryDining),
        ),
    ),
    MaterialIconSection(
        title = "Shopping",
        icons = listOf(
            MaterialIconOption("shopping_bag", "Shopping bag", Icons.Filled.ShoppingBag),
            MaterialIconOption("shopping_cart", "Shopping cart", Icons.Filled.ShoppingCart),
            MaterialIconOption("storefront", "Storefront", Icons.Filled.Storefront),
            MaterialIconOption("local_mall", "Mall", Icons.Filled.LocalMall),
            MaterialIconOption("sell", "Sale", Icons.Filled.Sell),
        ),
    ),
    MaterialIconSection(
        title = "Life",
        icons = listOf(
            MaterialIconOption("home", "Home", Icons.Filled.Home),
            MaterialIconOption("lightbulb", "Utilities", Icons.Filled.Lightbulb),
            MaterialIconOption("pets", "Pets", Icons.Filled.Category),
            MaterialIconOption("subscriptions", "Subscriptions", Icons.Filled.Subscriptions),
        ),
    ),
    MaterialIconSection(
        title = "Personal",
        icons = listOf(
            MaterialIconOption("person", "Personal", Icons.Filled.Person),
            MaterialIconOption("face", "Beauty", Icons.Filled.Face),
            MaterialIconOption("favorite", "Love", Icons.Filled.Favorite),
            MaterialIconOption("star", "Favorite", Icons.Filled.Star),
        ),
    ),
    MaterialIconSection(
        title = "Education",
        icons = listOf(
            MaterialIconOption("school", "School", Icons.Filled.School),
            MaterialIconOption("menu_book", "Books", Icons.AutoMirrored.Filled.MenuBook),
            MaterialIconOption("local_library", "Library", Icons.Filled.LocalLibrary),
            MaterialIconOption("calculate", "Tuition", Icons.Filled.Calculate),
            MaterialIconOption("science", "Science", Icons.Filled.Science),
        ),
    ),
    MaterialIconSection(
        title = "Festival",
        icons = listOf(
            MaterialIconOption("cake", "Cake", Icons.Filled.Cake),
            MaterialIconOption("redeem", "Gift", Icons.Filled.Redeem),
            MaterialIconOption("card_giftcard", "Card gift", Icons.Filled.CardGiftcard),
            MaterialIconOption("emoji_events", "Prize", Icons.Filled.EmojiEvents),
        ),
    ),
    MaterialIconSection(
        title = "Sports",
        icons = listOf(
            MaterialIconOption("fitness_center", "Gym", Icons.Filled.FitnessCenter),
            MaterialIconOption("sports_soccer", "Soccer", Icons.Filled.SportsSoccer),
            MaterialIconOption("sports_basketball", "Basketball", Icons.Filled.SportsBasketball),
            MaterialIconOption("directions_run", "Running", Icons.AutoMirrored.Filled.DirectionsRun),
        ),
    ),
    MaterialIconSection(
        title = "Office",
        icons = listOf(
            MaterialIconOption("work", "Work", Icons.Filled.Work),
            MaterialIconOption("business_center", "Business", Icons.Filled.BusinessCenter),
            MaterialIconOption("laptop_mac", "Laptop", Icons.Filled.LaptopMac),
            MaterialIconOption("print", "Print", Icons.Filled.Print),
        ),
    ),
    MaterialIconSection(
        title = "Transportation",
        icons = listOf(
            MaterialIconOption("directions_car", "Car", Icons.Filled.DirectionsCar),
            MaterialIconOption("local_gas_station", "Petrol", Icons.Filled.LocalGasStation),
            MaterialIconOption("local_parking", "Parking", Icons.Filled.LocalParking),
            MaterialIconOption("train", "Train", Icons.Filled.Train),
            MaterialIconOption("flight", "Flight", Icons.Filled.Flight),
        ),
    ),
    MaterialIconSection(
        title = "Health",
        icons = listOf(
            MaterialIconOption("medical_services", "Medical", Icons.Filled.MedicalServices),
            MaterialIconOption("local_hospital", "Hospital", Icons.Filled.LocalHospital),
            MaterialIconOption("healing", "Clinic", Icons.Filled.Healing),
            MaterialIconOption("monitor_heart", "Health", Icons.Filled.MonitorHeart),
        ),
    ),
    MaterialIconSection(
        title = "Travel",
        icons = listOf(
            MaterialIconOption("flight_takeoff", "Flight takeoff", Icons.Filled.FlightTakeoff),
            MaterialIconOption("hotel", "Hotel", Icons.Filled.Hotel),
            MaterialIconOption("beach_access", "Beach", Icons.Filled.BeachAccess),
        ),
    ),
    MaterialIconSection(
        title = "Finance",
        icons = listOf(
            MaterialIconOption("payments", "Payments", Icons.Filled.Payments),
            MaterialIconOption("account_balance_wallet", "Wallet", Icons.Filled.AccountBalanceWallet),
            MaterialIconOption("account_balance", "Bank", Icons.Filled.AccountBalance),
            MaterialIconOption("credit_card", "Credit card", Icons.Filled.CreditCard),
            MaterialIconOption("savings", "Savings", Icons.Filled.Savings),
            MaterialIconOption("undo", "Refund", Icons.AutoMirrored.Filled.Undo),
        ),
    ),
    MaterialIconSection(
        title = "Others",
        icons = listOf(
            MaterialIconOption("category", "Category", Icons.Filled.Category),
            MaterialIconOption("more_horiz", "Other", Icons.Filled.MoreHoriz),
            MaterialIconOption("help_outline", "Help", Icons.AutoMirrored.Filled.HelpOutline),
        ),
    ),
)

val MaterialIconOptions = MaterialIconSections.flatMap { it.icons }

val CategoryColorOptions = listOf(
    "#EF4444",
    "#F97316",
    "#F59E0B",
    "#EAB308",
    "#84CC16",
    "#22C55E",
    "#14B8A6",
    "#06B6D4",
    "#0EA5E9",
    "#3B82F6",
    "#6366F1",
    "#8B5CF6",
    "#A855F7",
    "#D946EF",
    "#EC4899",
    "#F43F5E",
    "#64748B",
    "#475569",
)

fun String.normalizedCategoryColor(fallback: String = "#64748B"): String {
    val normalized = trim().removePrefix("#")
    return if (normalized.length == 6 && normalized.all { it.isHexDigit() }) {
        "#${normalized.uppercase()}"
    } else {
        fallback
    }
}

fun String.toCategoryColor(): Color =
    Color(normalizedCategoryColor().removePrefix("#").let { "FF$it" }.toLong(16))

private fun Char.isHexDigit(): Boolean =
    this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

private val AppBackground = Color(0xFF08142A)
private val Panel = Color(0xFF101B37)
private val SelectedPanel = Color(0xFF1C4E8B)
private val ButtonPanel = Color(0xFF112A59)
private val Stroke = Color(0xFF243659)
private val Accent = Color(0xFF2F63D9)
private val AccentLight = Color(0xFF38A4F8)
private val MutedText = Color(0xFF8FB0E8)
