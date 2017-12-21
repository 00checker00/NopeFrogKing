package de.nopefrogking.utils

enum class FontIcon(val char: String) {
    Play("A"),
    Shop("B"),
    Highscore("C"),
    Progress("D"),
    Item_Flask("E"),
    Credits("F"),
    Exit("G"),
    Item_Orb("H"),
    Confirm("I"),
    Options("J"),
    Pause("K"),
    Repeat("L"),
    Item_Umbrella("M"),
    Items("N"),
    Item_Storm("O"),
    Info("P"),
    Menu("Q"),
    Home("R"),
    ArrowRight("S"),
    ArrowLeft("T"),
    Plus("U"),
    ExclamationMark("V"),
    Trashcan("W"),
    SoundEnabled("X"),
    SoundDisable("Y"),
    Present("Z"),

    Nothing("");

    operator fun invoke() = char
}
