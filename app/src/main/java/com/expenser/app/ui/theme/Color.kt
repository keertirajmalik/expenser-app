package com.expenser.app.ui.theme

import androidx.compose.ui.graphics.Color

// Ported from the web app's shadcn "Rose" theme (expenser-ui/src/index.css).

// Light
val RoseLightPrimary = Color(0xFFE11D48)
val RoseLightPrimaryFg = Color(0xFFFFF1F2)
// Bento layout: a soft grey ground with borderless white cards on it.
val RoseLightBackground = Color(0xFFF4F4F5)
val RoseLightForeground = Color(0xFF09090B)
val RoseLightCard = Color(0xFFFFFFFF)
val RoseLightSecondary = Color(0xFFF4F4F5)
val RoseLightSecondaryFg = Color(0xFF18181B)
val RoseLightMuted = Color(0xFFF4F4F5)
// #71717A is 4.37:1 on the #F4F4F5 ground - under AA for the 12-13sp captions that use
// it. #52525B is 7.0:1 on the ground and 7.7:1 on a white card.
val RoseLightMutedFg = Color(0xFF52525B)
// Text-weight destructive: #EF4444 on white is 3.76:1, under AA for the error captions
// that use it as text (e.g. "Replaces everything in the app"). #DC2626 is 4.83:1.
val RoseLightDestructive = Color(0xFFDC2626)
val RoseLightDestructiveFg = Color(0xFFFAFAFA)
val RoseLightBorder = Color(0xFFE4E4E7)

// Dark
val RoseDarkPrimary = Color(0xFFE11D48)
val RoseDarkPrimaryFg = Color(0xFFFFF1F2)
val RoseDarkBackground = Color(0xFF0C0A09)
val RoseDarkForeground = Color(0xFFF2F2F2)
val RoseDarkCard = Color(0xFF1C1917)
val RoseDarkSecondary = Color(0xFF27272A)
val RoseDarkSecondaryFg = Color(0xFFFAFAFA)
val RoseDarkMuted = Color(0xFF27272A)
val RoseDarkMutedFg = Color(0xFFA1A1AA)
// shadcn's dark --destructive (#7F1D1D) is a *container* colour. Material's `error` is
// also a foreground - error captions and the delete button paint text with it - and
// #7F1D1D on #0C0A09 is 1.97:1, below even the 3:1 large-text floor. Follow Material's
// dark convention instead: a light error with a dark onError. 7.34:1 on the background,
// and 5.81:1 for the icon on the swipe-to-delete fill.
val RoseDarkDestructive = Color(0xFFFB7185)
val RoseDarkDestructiveFg = Color(0xFF4C0519)
val RoseDarkBorder = Color(0xFF27272A)
