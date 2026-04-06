1. COLOR SYSTEM
   Primary brand color

Technogym Yellow

HEX:
#FFE01E

RGB:
255,224,30

Uso:

botones principales
highlights
indicadores de progreso
elementos activos
cursor input
iconos activos
barras de progreso
elementos seleccionados

Referencia de color oficial Technogym:
#FFE01E

Base dark palette

Background main
#0B0B0B

Surface
#121212

Card
#1E1E1E

Border subtle
#2A2A2A

Divider
#333333

Hover
#242424

Text colors

Primary text
#FFFFFF

Secondary text
#A0A0A0

Muted text
#6F6F6F

Disabled text
#4A4A4A

Semantic colors

Success
#22C55E

Warning
#F59E0B

Error
#EF4444

Info
#3B82F6

2. TYPOGRAPHY

Font style:

Sans serif moderna.

Android:
Material default

Web:
Inter
Roboto
System font

Hierarchy:

H1
24sp
bold

H2
20sp
semi-bold

H3
18sp
medium

Body
16sp
regular

Secondary text
14sp

Small text
12sp

Button text
16sp
semi-bold

3. GLOBAL LAYOUT

Spacing system (8dp grid)

4
8
12
16
20
24
32
40

Standard padding screen:

16dp horizontal

24dp vertical sections

Card padding:

16dp

Gap between components:

12dp

4. COMPONENT DESIGN
   Buttons

Primary button

background:
#FFE01E

text:
#000000

border radius:
12dp

height:
48dp

padding horizontal:
20dp

shadow:
subtle

Secondary button

background:
transparent

border:
1dp #2A2A2A

text:
#FFFFFF

Ghost button

background:
transparent

text:
#FFE01E

Disabled button

background:
#2A2A2A

text:
#6F6F6F

Cards

background:
#1E1E1E

radius:
16dp

border:
1dp #2A2A2A

padding:
16dp

shadow:
very subtle

Input fields

background:
#121212

border:
1dp #2A2A2A

focus border:
#FFE01E

text:
#FFFFFF

placeholder:
#6F6F6F

radius:
12dp

height:
52dp

padding:
16dp

cursor:
#FFE01E

5. CHAT SCREEN DESIGN

Pantalla principal del producto.

Debe ser visualmente limpia.

Layout:

Top bar
Chat messages
Input bar fijo abajo

Top bar

background:
#0B0B0B

title:
Fitness AI Coach

text color:
#FFFFFF

right icon:
profile button

icon color:
#FFE01E

height:
56dp

divider bottom:
1dp #2A2A2A

Chat background

background:
#0B0B0B

User message bubble

background:
#1E1E1E

text:
#FFFFFF

radius:
16dp

padding:
12dp

max width:
80%

alignment:
right

AI message bubble

background:
#1E1E1E

left border accent:
4dp #FFE01E

text:
#FFFFFF

radius:
16dp

padding:
12dp

alignment:
left

Chat input bar

background:
#0B0B0B

border top:
1dp #2A2A2A

padding:
12dp

Chat text input

background:
#121212

border:
1dp #2A2A2A

focus border:
#FFE01E

radius:
24dp

padding:
14dp

text:
#FFFFFF

Send button

shape:
circle

size:
48dp

background:
#FFE01E

icon color:
#000000

icon:
send

6. PROFILE SCREEN

Layout vertical scroll.

Sections:

User info
Goal
Macros target
Calories target

Section title

text:
#FFFFFF

size:
18sp

margin bottom:
8dp

Metric card

background:
#1E1E1E

radius:
16dp

padding:
16dp

Progress bar

background:
#2A2A2A

progress color:
#FFE01E

height:
8dp

radius:
8dp

Example:

Calories progress

Protein progress

Carbs progress

Fat progress

7. DAILY PROGRESS COMPONENT

Card showing:

calories consumed
calories remaining
steps
training status

Layout:

vertical metrics list

Metric item:

label small
value big

Example:

Calories
1800 / 2200

Steps
8400

Value text:

color:
#FFE01E

size:
20sp

weight:
bold

8. ICON STYLE

Icons:

minimal
outlined
monochrome

default color:
#A0A0A0

active color:
#FFE01E

size:

24dp standard

Recommended icons:

chat
profile
weight
fire (calories)
steps
dumbbell
nutrition
progress chart

9. ANIMATIONS

Durations:

fast:
150ms

normal:
250ms

slow:
350ms

Effects:

fade in messages
button press scale 0.97
smooth progress animation

10. ANDROID COMPOSE THEME

Create file:

ui/theme/Color.kt

val YellowPrimary = Color(0xFFFFE01E)

val BackgroundMain = Color(0xFF0B0B0B)
val Surface = Color(0xFF121212)
val Card = Color(0xFF1E1E1E)

val Border = Color(0xFF2A2A2A)
val Divider = Color(0xFF333333)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA0A0A0)
val TextMuted = Color(0xFF6F6F6F)

val Success = Color(0xFF22C55E)
val Warning = Color(0xFFF59E0B)
val Error = Color(0xFFEF4444)
11. UI PRIORITY ORDER

Implement first:

1 Chat screen
2 Input component
3 Message bubbles
4 Profile screen
5 Progress cards
6 Buttons
7 Theme system

12. DESIGN RULES

Use few colors.

Use contrast.

Avoid gradients.

Avoid many accent colors.

Use yellow only for:

important actions
progress
highlights

Most UI should be dark gray.

13. VISUAL GOAL

The UI should feel similar to:

premium fitness apps
modern AI apps
high-end gym equipment interfaces

User should feel:

clarity
control
motivation
professional product quality