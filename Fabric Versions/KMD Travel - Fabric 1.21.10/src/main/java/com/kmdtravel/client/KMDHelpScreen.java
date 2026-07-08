package com.kmdtravel.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class KMDHelpScreen extends KMDScreen {
    private static final HelpPage[] PAGES = {
            new HelpPage("Start", List.of(
                    section("What KMD Travel is", "KMD Travel adds Kingdom Come: Deliverance style fast travel to Minecraft. Players discover travel posts, open a parchment map, choose a discovered location, and may be interrupted by configurable events."),
                    section("Basic loop", "Place a travel post, right click it to discover it, place or discover another post, then right click a discovered post to open the fast travel map."),
                    section("Important rule", "Normal travel posts are personal discovery. A player must discover them before seeing or using them. Shared travel posts are public and show for everyone without discovery.")
            )),
            new HelpPage("Posts", List.of(
                    section("Travel posts", "Regular posts are discovered per player. The player who places one discovers it automatically. Breaking the post removes its marker from the map."),
                    section("Shared travel posts", "Shared posts are fancier public posts. Everyone can see them on the map. Crafting can be toggled by admins with /kmdtravel sharedpostcrafting true or false."),
                    section("Editing posts", "Shift right click a post to rename it. Right click with dye to recolor sign text. Right click with a banner to change the map pin shield design for that post."),
                    example("Admin setting", "/kmdtravel sharedpostcrafting false")
            )),
            new HelpPage("Map", List.of(
                    section("Opening the map", "Right click a discovered travel post. The map only shows locations valid for that player and dimension. Shared posts show for everyone."),
                    section("Controls", "Drag the parchment to pan. Mouse wheel zooms. The discovered areas panel can be toggled, and clicking an entry moves the map to that post."),
                    section("Map cache", "KMD keeps discovered map tile data in the client modpack folder under kmdtravel/map-cache so explored areas can be shown later. The map updates nearby loaded chunks while you play.")
            )),
            new HelpPage("Profiles", List.of(
                    section("Event profiles", "A profile is a complete set of events. Servers can keep multiple profiles, then assign one globally or to specific players."),
                    section("Default profile", "The global/default profile is used by players who do not have a personal profile override. If no profiles exist, no custom events are selected."),
                    section("JSON export", "Profiles are mirrored to JSON in the modpack folder at kmdtravel/events/<world-name>. In-game edits update that world's files, and deleting a profile in-game deletes its JSON too. Mob NBT is written as plain text in each mob object's nbt field."),
                    example("Profile commands", "/kmdtravel eventprofiles list\n/kmdtravel eventprofiles global hard_roads\n/kmdtravel eventprofiles player Steve peaceful_roads")
            )),
            new HelpPage("Events", List.of(
                    section("How events are picked", "When fast travel is interrupted, KMD checks the player's active profile. Enabled events that match dimension, biome, time of day, and route conditions enter the random pool."),
                    section("Passive events", "Passive events show a countdown above the hotbar. When the timer finishes, travel continues. They are good for merchants, travelers, camps, discoveries, and flavor moments."),
                    section("Aggressive events", "Aggressive events spawn danger. Ends: Kill waits until tracked mobs die. Ends: Timed resumes after the event duration."),
                    section("Chance fields", "Selection weight controls how often this event is chosen compared to other matching events. Avoid chance is the base chance to skip that specific event."),
                    example("Simple event idea", "Title: Road Bandits\nSubtitle: A group blocks the road ahead.\nAggressive: true\nEnds: Kill\nWeight: 1.0\nAvoid chance: 0.15")
            )),
            new HelpPage("Fields", List.of(
                    section("Event ID", "The saved key for the event, such as road_bandits. Keep it lowercase and simple."),
                    section("Title and subtitle", "Title is the main travel screen text. Subtitle is the description under it. These do not rename mobs."),
                    section("Dimension and biome", "Blank means global. If dimension is minecraft:overworld and biome is minecraft:beach, the event only appears on matching overworld beach routes."),
                    section("Duration", "Passive events use duration as countdown time. Aggressive timed events use it as the time until travel resumes. Aggressive kill events finish from tracked mob deaths."),
                    example("Dimension examples", "minecraft:overworld\nminecraft:the_nether\nminecraft:the_end\nmodid:custom_dimension")
            )),
            new HelpPage("Mobs", List.of(
                    section("Mob rows", "Mob ID is the entity registry id. Amount is exact count. Spawn Range is how far from the traveler KMD tries to place the mob."),
                    section("Modded mobs", "Modded mobs work if you use their registry id, for example modid:troll. The dropdown shows known mobs, but you can also type manually."),
                    section("Kill tracking", "Mobs added through the mob list are tracked automatically for Ends: Kill. Command-spawned mobs need Tags:[\"{event_tag}\"]."),
                    example("Mob row example", "Mob ID: minecraft:skeleton\nAmount: 3\nSpawn Range: 18\nMob display name: Road Archer\nNBT: PersistenceRequired:1b")
            )),
            new HelpPage("NBT", List.of(
                    section("What NBT is", "NBT/SNBT is Minecraft entity data. KMD accepts either a full compound with braces or only the inside. NoAI:1b and {NoAI:1b} both work."),
                    section("Common tags", "NoAI:1b disables AI. PersistenceRequired:1b prevents despawn. Health:40f sets health. CustomName sets a JSON text name."),
                    section("1.21 item syntax", "Use lowercase count in item stacks. ArmorItems order is boots, leggings, chestplate, helmet. HandItems order is main hand, off hand."),
                    example("Named sword mob", "CustomName:'{\"text\":\"Bandit Captain\",\"color\":\"gold\"}',PersistenceRequired:1b,HandItems:[{id:\"minecraft:iron_sword\",count:1b},{id:\"minecraft:air\",count:0b}]"),
                    example("No AI statue", "NoAI:1b,PersistenceRequired:1b,CustomName:'{\"text\":\"Frozen Guard\",\"color\":\"gray\"}'")
            )),
            new HelpPage("Commands", List.of(
                    section("Command rows", "Commands run when the event starts. Delay belongs to that command row. Do not type a leading slash."),
                    section("Safe targeting", "Use {player} instead of @p. In multiplayer, @p can target the wrong nearby player. {player} is always the traveler who triggered the event."),
                    section("Manual finish", "Use kmdtravel finishencounter from chat, a command block, or a quest reward to end a player's active encounter, flush remaining event command rows, and resume fast travel. Do not put finishencounter inside the event command list."),
                    section("Sound categories", "playsound needs a valid category, such as hostile, ambient, player, music, weather, block, record, neutral, master, or voice."),
                    example("Sound", "playsound minecraft:entity.ghast.scream hostile {player} {x} {y} {z} 1 0.4"),
                    example("Effects and titles", "effect give {player} minecraft:slowness 10 1\ntitle {player} actionbar {\"text\":\"Ambush!\",\"color\":\"red\"}"),
                    example("Finish encounter", "kmdtravel finishencounter Steve\nkmdtravel finishencounter @p\nkmdtravel finishencounter @a")
            )),
            new HelpPage("Tags", List.of(
                    section("Placeholders", "{player} is the traveler name. {uuid} is the traveler UUID. {x} {y} {z} are event spawn coordinates. {event_tag} is unique for this one event instance."),
                    section("Event tag use", "If a command summons mobs and the event uses Ends: Kill, include Tags:[\"{event_tag}\"] so KMD knows those mobs belong to that event."),
                    section("Two players at once", "Unique event tags keep Player A's mobs from finishing Player B's event, even if both trigger the same event at the same time."),
                    example("Tracked summon", "summon minecraft:zombie {x} {y} {z} {Tags:[\"{event_tag}\"],CustomName:'{\"text\":\"Tagged Bandit\"}'}")
            )),
            new HelpPage("Admin", List.of(
                    section("Opening tools", "/kmdtravel help opens this manual. /kmdtravel events opens the event profile editor for admins."),
                    section("Persistent config commands", "Event chance and shared post crafting commands save to config/kmdtravel/kmdtravel-common.toml, so they persist after world/server restart."),
                    section("Profile commands", "Use eventprofiles global to change the server default. Use eventprofiles player to set one player only."),
                    example("Useful admin commands", "/kmdtravel events\n/kmdtravel basechance 0.25\n/kmdtravel maxchance 0.75\n/kmdtravel eventprofiles current\n/kmdtravel sharedpostcrafting true")
            )),
            new HelpPage("Cmd Ref", List.of(
                    section("/kmdtravel help", "Opens this manual for the player running it."),
                    section("/kmdtravel events [true|false]", "Without true/false, admins open the event editor UI. With true/false, it toggles whether travel interruptions can happen at all."),
                    section("/kmdtravel eventwait <5-600>", "Sets the default passive event countdown in seconds. Individual custom passive events can override it with Duration Seconds."),
                    section("/kmdtravel basechance <0-1>", "Sets the starting chance for any travel interruption before modifiers. Example: 0.25 means 25%."),
                    section("/kmdtravel nightmultiplier <0-10>", "Multiplies base chance when travel starts at night. Example: base 0.25 and night 2.0 becomes 0.50 before later modifiers."),
                    section("/kmdtravel armor perpoint <0-0.1>", "Reduces event chance using total armor and armor toughness, including modded armor attributes. It affects whether an event happens and the displayed skip chance fallback."),
                    section("/kmdtravel armor maxreduction <0-0.95>", "Caps armor's total reduction. Example: 0.75 means armor can reduce event chance by at most 75%."),
                    section("/kmdtravel hunger low <0-1>", "Adds chance when food is 6 or below. Example: 0.20 adds 20 percentage points."),
                    section("/kmdtravel hunger medium <0-1>", "Adds chance when food is 12 or below, but above low hunger."),
                    section("/kmdtravel distancechance <0-1>", "Adds chance per 1000 blocks traveled. Example: 0.10 adds 10 percentage points per 1000 blocks."),
                    section("/kmdtravel maxchance <0-1>", "Caps final ambush/interruption chance after all modifiers."),
                    section("/kmdtravel finishencounter <target>", "Finishes the active KMD encounter for selected player targets, runs remaining event command rows immediately, and resumes fast travel. Use player names or vanilla selectors like @p, @a, or @e[type=minecraft:player,limit=1,sort=nearest]. The {player} placeholder is only for normal event command rows and is not valid in command blocks."),
                    section("/kmdtravel sharedpostcrafting [true|false]", "Shows or toggles whether shared travel posts can be crafted."),
                    section("/kmdtravel eventprofiles list", "Lists all profiles and marks the current global/default profile."),
                    section("/kmdtravel eventprofiles global <id>", "Sets the default profile for players without a personal override."),
                    section("/kmdtravel eventprofiles player <player> <id>", "Sets a specific player's active event profile."),
                    section("/kmdtravel eventprofiles current [player]", "Shows the global profile, or the selected player's active profile."),
                    section("Ambush % formula", "Final chance = base chance, multiplied by night multiplier if night, multiplied by (1 - armor reduction), then hunger and distance bonuses are added, then maxchance caps it. Armor reduction = (armor + toughness x 0.5) x armor perpoint, capped by armor maxreduction."),
                    example("Chance example", "basechance 0.25\nnightmultiplier 2.0\narmor perpoint 0.025\narmor maxreduction 0.75\ndistancechance 0.10\nmaxchance 0.95")
            ))
    };

    private int page;
    private int scroll;

    public KMDHelpScreen() {
        super(Component.literal("KMD Travel Help"));
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose()).bounds(width / 2 - 60, height - 28, 120, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF15100A);
        int panelX = 24;
        int panelY = 30;
        int panelW = width - 48;
        int panelH = height - 66;
        int tabY = panelY + 24;
        int tabsBottom = tabsBottom(panelX + 10, tabY, panelW - 20);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF1F160D);
        graphics.fill(panelX, panelY, panelX + panelW, tabsBottom + 6, 0xFF5A3A1E);
        graphics.drawString(font, "KMD Travel Manual", panelX + 10, panelY + 8, 0xFFFFE6B0, false);
        drawTabs(graphics, mouseX, mouseY, panelX + 10, tabY, panelW - 20);
        renderPage(graphics, panelX + 14, tabsBottom + 18, panelW - 28, panelY + panelH - tabsBottom - 28);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    private void drawTabs(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int maxWidth) {
        int tabX = x;
        int tabY = y;
        for (int i = 0; i < PAGES.length; i++) {
            int tabW = Math.max(52, font.width(PAGES[i].title()) + 14);
            if (tabX + tabW > x + maxWidth) {
                tabX = x;
                tabY += 16;
            }
            boolean hot = mouseX >= tabX && mouseX <= tabX + tabW && mouseY >= tabY && mouseY <= tabY + 14;
            int color = i == page ? 0xFF9B733D : hot ? 0xFF563821 : 0xFF2C1D10;
            graphics.fill(tabX, tabY, tabX + tabW, tabY + 14, color);
            graphics.drawString(font, PAGES[i].title(), tabX + 7, tabY + 4, 0xFFFFE6B0, false);
            tabX += tabW + 4;
        }
    }

    private int tabsBottom(int x, int y, int maxWidth) {
        int tabX = x;
        int tabY = y;
        for (HelpPage page : PAGES) {
            int tabW = Math.max(52, font.width(page.title()) + 14);
            if (tabX + tabW > x + maxWidth) {
                tabX = x;
                tabY += 16;
            }
            tabX += tabW + 4;
        }
        return tabY + 14;
    }

    private void renderPage(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.enableScissor(x - 2, y - 2, x + width + 2, y + height + 2);
        int lineY = y - scroll;
        for (HelpSection section : PAGES[page].sections()) {
            if (section.example()) {
                lineY = drawExample(graphics, section.header(), section.text(), x, lineY, width);
            } else {
                lineY = drawSection(graphics, section.header(), section.text(), x, lineY, width);
            }
            lineY += 8;
        }
        graphics.disableScissor();
    }

    private int drawSection(GuiGraphics graphics, String header, String text, int x, int y, int width) {
        graphics.drawString(font, header, x, y, 0xFFFFD36A, false);
        int lineY = y + 13;
        for (FormattedCharSequence line : font.split(Component.literal(text), width - 12)) {
            graphics.drawString(font, line, x + 10, lineY, 0xFFFFE6B0, false);
            lineY += 11;
        }
        return lineY;
    }

    private int drawExample(GuiGraphics graphics, String header, String text, int x, int y, int width) {
        graphics.drawString(font, header, x, y, 0xFFFFD36A, false);
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (String rawLine : text.split("\\n")) {
            lines.addAll(font.split(Component.literal(rawLine), width - 32));
        }
        int boxTop = y + 13;
        int boxBottom = boxTop + lines.size() * 11 + 8;
        graphics.fill(x + 10, boxTop, x + width - 8, boxBottom, 0xFF090603);
        int lineY = boxTop + 5;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, x + 16, lineY, 0xFFBFEFFF, false);
            lineY += 11;
        }
        return boxBottom;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        int panelX = 24;
        int panelY = 30;
        int x = panelX + 10;
        int tabX = x;
        int tabY = panelY + 24;
        int maxWidth = width - 68;
        for (int i = 0; i < PAGES.length; i++) {
            int tabW = Math.max(52, font.width(PAGES[i].title()) + 14);
            if (tabX + tabW > x + maxWidth) {
                tabX = x;
                tabY += 16;
            }
            if (mouseX >= tabX && mouseX <= tabX + tabW && mouseY >= tabY && mouseY <= tabY + 14) {
                page = i;
                scroll = 0;
                return true;
            }
            tabX += tabW + 4;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, scroll - (int) (scrollY * 18));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static HelpSection section(String header, String text) {
        return new HelpSection(header, text, false);
    }

    private static HelpSection example(String header, String text) {
        return new HelpSection(header, text, true);
    }

    private record HelpPage(String title, List<HelpSection> sections) {
    }

    private record HelpSection(String header, String text, boolean example) {
    }
}




