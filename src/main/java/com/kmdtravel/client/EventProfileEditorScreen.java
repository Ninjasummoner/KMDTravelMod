package com.kmdtravel.client;

import com.kmdtravel.eventconfig.AggressiveCompletion;
import com.kmdtravel.eventconfig.EditableTravelEvent;
import com.kmdtravel.eventconfig.EventCommandStep;
import com.kmdtravel.eventconfig.EventProfile;
import com.kmdtravel.eventconfig.EventTimeOfDay;
import com.kmdtravel.network.KMDNetwork;
import com.kmdtravel.network.OpenEventProfileEditorPacket;
import com.kmdtravel.network.SaveEventProfilePacket;
import com.kmdtravel.network.SetDefaultEventProfilePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

public class EventProfileEditorScreen extends KMDScreen {
    private enum View {
        PROFILES,
        EVENTS,
        EDIT_EVENT
    }

    private final List<String> mobs;
    private final List<String> dimensions;
    private final List<String> biomes;
    private final List<EventProfile> profiles = new ArrayList<>();
    private View view = View.PROFILES;
    private int profileIndex;
    private int eventIndex = -1;
    private int scroll;
    private long lastProfileClick;
    private int lastProfileIndex = -1;
    private long lastEventClick;
    private int lastEventIndex = -1;
    private EditBox profileNameInput;
    private EditBox idInput;
    private EditBox titleInput;
    private EditBox descriptionInput;
    private EditBox dimensionInput;
    private EditBox biomeInput;
    private EditBox durationInput;
    private EditBox weightInput;
    private EditBox avoidInput;
    private EditBox mobInput;
    private EditBox mobAmountInput;
    private EditBox mobNameInput;
    private EditBox mobNbtInput;
    private EditBox mobRangeInput;
    private EditBox commandInput;
    private EditBox commandDelayInput;
    private boolean enabled = true;
    private boolean passive;
    private AggressiveCompletion aggressiveCompletion = AggressiveCompletion.KILL_MOBS;
    private EventTimeOfDay timeOfDay = EventTimeOfDay.BOTH;
    private final List<String> editMobs = new ArrayList<>();
    private final List<EventCommandStep> editCommands = new ArrayList<>();
    private boolean mobDropdownOpen;
    private boolean dimensionDropdownOpen;
    private boolean biomeDropdownOpen;
    private boolean helpOpen;
    private int helpPage;
    private int mobDropdownScroll;
    private int dimensionDropdownScroll;
    private int biomeDropdownScroll;
    private int addedMobScroll;
    private int commandListScroll;
    private int selectedMobIndex;
    private int selectedAddedMobIndex = -1;
    private int selectedCommandIndex = -1;
    private String editId = "new_event";
    private String editTitle = "New Event";
    private String editDescription = "A custom event happens.";
    private String editDimension = "";
    private String editBiome = "";
    private String editDuration = "60";
    private String editWeight = "1.0";
    private String editAvoid = "0.0";
    private String editMobInput = "";
    private String editMobAmount = "1";
    private String editMobName = "";
    private String editMobNbt = "";
    private String editMobRange = "12";
    private String editCommandInput = "";
    private String editCommandDelay = "0";
    private String lastDimensionValue = "";

    public EventProfileEditorScreen(OpenEventProfileEditorPacket packet) {
        super(Component.literal("KMD Event Profiles"));
        this.mobs = packet.mobs();
        this.dimensions = packet.dimensions();
        this.biomes = packet.biomes();
        ListTag profilesTag = packet.profiles().getList("Profiles", 10);
        for (int i = 0; i < profilesTag.size(); i++) {
            profiles.add(EventProfile.load(profilesTag.getCompound(i)));
        }
        profiles.sort(Comparator.comparing(EventProfile::id));
    }

    @Override
    protected void init() {
        clearWidgets();
        if (view == View.PROFILES) {
            initProfiles();
        } else if (view == View.EVENTS) {
            initEvents();
        } else {
            initEventEditor();
        }
    }

    private void initProfiles() {
        int left = width / 2 - 150;
        addRenderableWidget(Button.builder(Component.literal("Create"), button -> createProfile()).bounds(left, 28, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Open"), button -> openSelectedProfile()).bounds(left + 76, 28, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Delete"), button -> deleteProfile()).bounds(left + 152, 28, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Set Default"), button -> setDefaultProfile()).bounds(left + 228, 28, 92, 20).build());
        addRenderableWidget(Button.builder(Component.literal(helpOpen ? "Help <" : "Help >"), button -> {
            helpOpen = !helpOpen;
            button.setMessage(Component.literal(helpOpen ? "Help <" : "Help >"));
        }).bounds(left + 326, 28, 78, 20).build());
        profileNameInput = new EditBox(font, left, height - 38, 210, 20, Component.literal("Profile name"));
        profileNameInput.setMaxLength(48);
        if (!profiles.isEmpty()) {
            profileNameInput.setValue(profiles.get(Math.max(0, Math.min(profileIndex, profiles.size() - 1))).name());
        }
        addRenderableWidget(profileNameInput);
        addRenderableWidget(Button.builder(Component.literal("Save Name"), button -> saveProfileName()).bounds(left + 218, height - 38, 92, 20).build());
    }

    private void initEvents() {
        int left = width / 2 - 170;
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> {
            view = View.PROFILES;
            scroll = 0;
            rebuild();
        }).bounds(left, 28, 58, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Create Event"), button -> createEvent()).bounds(left + 64, 28, 96, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Edit"), button -> editSelectedEvent()).bounds(left + 166, 28, 58, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Save Profile"), button -> saveSelectedProfile()).bounds(left + 230, 28, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal(helpOpen ? "Help <" : "Help >"), button -> {
            helpOpen = !helpOpen;
            button.setMessage(Component.literal(helpOpen ? "Help <" : "Help >"));
        }).bounds(left + 334, 28, 78, 20).build());
    }

    private void initEventEditor() {
        int left = editorLeft();
        int y = 76;
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> {
            view = View.EVENTS;
            scroll = 0;
            rebuild();
        }).bounds(left, 28, 58, 20).build());
        addRenderableWidget(Button.builder(Component.literal(enabled ? "Enabled" : "Disabled"), button -> {
            enabled = !enabled;
            button.setMessage(Component.literal(enabled ? "Enabled" : "Disabled"));
        }).bounds(left + 76, 28, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal(passive ? "Passive" : "Aggressive"), button -> {
            captureEditorFields();
            passive = !passive;
            rebuild();
        }).bounds(left + 174, 28, 104, 20).build());
        if (!passive) {
            addRenderableWidget(Button.builder(Component.literal("Ends: " + completionLabel()), button -> {
                aggressiveCompletion = aggressiveCompletion == AggressiveCompletion.KILL_MOBS ? AggressiveCompletion.TIMED : AggressiveCompletion.KILL_MOBS;
                button.setMessage(Component.literal("Ends: " + completionLabel()));
            }).bounds(left + 286, 28, 110, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal(helpOpen ? "Help <" : "Help >"), button -> {
            helpOpen = !helpOpen;
            button.setMessage(Component.literal(helpOpen ? "Help <" : "Help >"));
        }).bounds(left + 410, 28, 78, 20).build());

        idInput = input(left, y, 190, "id"); titleInput = input(left + 210, y, 410, "title"); y += 38;
        descriptionInput = input(left, y, 620, "subtitle shown on travel screen"); y += 38;
        dimensionInput = input(left, y, 260, "dimension blank=global");
        Button dimensionButton = addRenderableWidget(Button.builder(Component.literal(dimensionDropdownOpen ? "^" : "v"), button -> {
            dimensionDropdownOpen = !dimensionDropdownOpen;
            biomeDropdownOpen = false;
            button.setMessage(Component.literal(dimensionDropdownOpen ? "^" : "v"));
        }).bounds(left + 265, y, 35, 20).build());
        biomeInput = input(left + 320, y, 260, "biome blank=global");
        biomeInput.active = !editDimension.isBlank();
        addRenderableWidget(Button.builder(Component.literal(biomeDropdownOpen ? "^" : "v"), button -> {
            if (dimensionInput != null && !dimensionInput.getValue().trim().isBlank()) {
                biomeDropdownOpen = !biomeDropdownOpen;
                dimensionDropdownOpen = false;
                button.setMessage(Component.literal(biomeDropdownOpen ? "^" : "v"));
                dimensionButton.setMessage(Component.literal("v"));
            }
        }).bounds(left + 585, y, 35, 20).build());
        y += 38;
        durationInput = input(left, y, 110, "duration"); weightInput = input(left + 130, y, 110, "weight"); avoidInput = input(left + 260, y, 110, "avoid 0-1");
        idInput.setValue(editId);
        titleInput.setValue(editTitle);
        descriptionInput.setValue(editDescription);
        dimensionInput.setValue(editDimension);
        biomeInput.setValue(editBiome);
        durationInput.setValue(editDuration);
        weightInput.setValue(editWeight);
        avoidInput.setValue(editAvoid);
        lastDimensionValue = editDimension;
        addRenderableWidget(Button.builder(Component.literal("Time: " + timeOfDay.name()), button -> {
            timeOfDay = timeOfDay == EventTimeOfDay.BOTH ? EventTimeOfDay.DAY : timeOfDay == EventTimeOfDay.DAY ? EventTimeOfDay.NIGHT : EventTimeOfDay.BOTH;
            button.setMessage(Component.literal("Time: " + timeOfDay.name()));
        }).bounds(left + 390, y, 230, 20).build());
        y += 42;
        mobInput = input(left, y, 300, "minecraft:zombie");
        mobInput.setValue(editMobInput);
        mobAmountInput = input(left + 310, y, 48, "amt");
        mobAmountInput.setValue(editMobAmount);
        mobRangeInput = input(left + 368, y, 56, "range");
        mobRangeInput.setValue(editMobRange);
        addRenderableWidget(Button.builder(Component.literal(mobDropdownOpen ? "Mobs ^" : "Mobs v"), button -> {
            mobDropdownOpen = !mobDropdownOpen;
            button.setMessage(Component.literal(mobDropdownOpen ? "Mobs ^" : "Mobs v"));
        }).bounds(left + 432, y, 76, 20).build());
        addRenderableWidget(Button.builder(Component.literal(selectedAddedMobIndex >= 0 ? "Edit" : "Add"), button -> addSelectedMob()).bounds(left + 516, y, 46, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Remove"), button -> removeSelectedMob()).bounds(left + 570, y, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("New"), button -> clearMobSelection()).bounds(left + 648, y, 44, 20).build());
        y += 34;
        mobNameInput = input(left, y, 220, "mob name optional");
        mobNameInput.setValue(editMobName);
        mobNbtInput = input(left + 230, y, 410, "NBT optional e.g. {Health:40f}");
        mobNbtInput.setValue(editMobNbt);
        y = commandInputY();
        int commandLeft = commandLeft();
        commandInput = input(commandLeft, y, commandPanelWidth(), "command");
        commandInput.setValue(editCommandInput);
        int commandButtonY = y + 36;
        commandDelayInput = input(commandLeft, commandButtonY, 60, "delay");
        commandDelayInput.setValue(editCommandDelay);
        addRenderableWidget(Button.builder(Component.literal(selectedCommandIndex >= 0 ? "Edit" : "Add"), button -> addCommand()).bounds(commandLeft + 72, commandButtonY, 44, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Remove"), button -> removeSelectedCommand()).bounds(commandLeft + 124, commandButtonY, 74, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Up"), button -> moveSelectedCommand(-1)).bounds(commandLeft + 206, commandButtonY, 38, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Down"), button -> moveSelectedCommand(1)).bounds(commandLeft + 252, commandButtonY, 52, 20).build());
        addRenderableWidget(Button.builder(Component.literal("New"), button -> clearCommandSelection()).bounds(commandLeft + 312, commandButtonY, 44, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Save Event"), button -> saveEditedEvent()).bounds(left, height - 30, 728, 24).build());
    }

    private int commandInputY() {
        return height - 118;
    }

    private int commandLeft() {
        return 20;
    }

    private int commandPanelWidth() {
        int mainLeft = editorLeft();
        return Math.min(626, Math.max(260, mainLeft - commandLeft() - 18));
    }

    private int commandListY() {
        return 60;
    }

    private int commandListHeight() {
        return Math.max(80, commandInputY() - commandListY() - 36);
    }

    private int mobsPanelTop() {
        return 294;
    }

    private int editorLeft() {
        return width / 2 - 250;
    }

    private int mobPanelHeight() {
        return 62;
    }

    private EditBox input(int x, int y, int w, String hint) {
        EditBox box = new EditBox(font, x, y, w, 20, Component.literal(hint));
        box.setMaxLength(4096);
        addRenderableWidget(box);
        return box;
    }

    private void rebuild() {
        init(minecraft, width, height);
    }

    @Override
    public void tick() {
        super.tick();
        if (view == View.EDIT_EVENT && dimensionInput != null && biomeInput != null) {
            String currentDimension = dimensionInput.getValue().trim();
            if (!currentDimension.equals(lastDimensionValue)) {
                lastDimensionValue = currentDimension;
                biomeInput.setValue("");
                editBiome = "";
                biomeDropdownScroll = 0;
                biomeDropdownOpen = false;
            }
            biomeInput.active = !currentDimension.isBlank();
            if (currentDimension.isBlank() && !biomeInput.getValue().isBlank()) {
                biomeInput.setValue("");
                editBiome = "";
            }
        }
    }

    private EventProfile selectedProfile() {
        return profiles.get(Math.max(0, Math.min(profileIndex, profiles.size() - 1)));
    }

    private void createProfile() {
        String id = "profile_" + (profiles.size() + 1);
        profiles.add(new EventProfile(id, "New Profile", List.of()));
        profileIndex = profiles.size() - 1;
        saveSelectedProfile();
        rebuild();
    }

    private void openSelectedProfile() {
        if (!profiles.isEmpty()) {
            view = View.EVENTS;
            eventIndex = -1;
            scroll = 0;
            rebuild();
        }
    }

    private void saveProfileName() {
        if (profiles.isEmpty()) {
            return;
        }
        EventProfile profile = selectedProfile();
        profiles.set(profileIndex, profile.withName(profileNameInput.getValue().trim().isBlank() ? profile.id() : profileNameInput.getValue().trim()));
        saveSelectedProfile();
    }

    private void deleteProfile() {
        if (profiles.isEmpty()) {
            return;
        }
        EventProfile profile = selectedProfile();
        KMDNetwork.sendToServer(new SaveEventProfilePacket(profile.save(), true));
        profiles.remove(profileIndex);
        profileIndex = Math.max(0, Math.min(profileIndex, profiles.size() - 1));
        rebuild();
    }

    private void saveSelectedProfile() {
        if (!profiles.isEmpty()) {
            KMDNetwork.sendToServer(new SaveEventProfilePacket(selectedProfile().save(), false));
        }
    }

    private void setDefaultProfile() {
        if (!profiles.isEmpty()) {
            KMDNetwork.sendToServer(new SetDefaultEventProfilePacket(selectedProfile().id()));
        }
    }

    private void createEvent() {
        loadEvent(new EditableTravelEvent("new_event", true, "New Event", false, 60, "A custom event happens.", "", "", EventTimeOfDay.BOTH, AggressiveCompletion.KILL_MOBS, 1.0D, 0.0D, List.of(), List.of()));
        eventIndex = -1;
        view = View.EDIT_EVENT;
        rebuild();
    }

    private void editSelectedEvent() {
        if (eventIndex >= 0 && eventIndex < selectedProfile().events().size()) {
            loadEvent(selectedProfile().events().get(eventIndex));
            view = View.EDIT_EVENT;
            rebuild();
        }
    }

    private void deleteSelectedEvent() {
        if (eventIndex < 0 || eventIndex >= selectedProfile().events().size()) {
            return;
        }
        EventProfile profile = selectedProfile();
        List<EditableTravelEvent> events = new ArrayList<>(profile.events());
        events.remove(eventIndex);
        profiles.set(profileIndex, profile.withEvents(events));
        eventIndex = -1;
        saveSelectedProfile();
        rebuild();
    }

    private void loadEvent(EditableTravelEvent event) {
        enabled = event.enabled();
        passive = event.passive();
        aggressiveCompletion = event.aggressiveCompletion();
        timeOfDay = event.timeOfDay();
        editId = event.id();
        editTitle = event.title();
        editDescription = event.description();
        editDimension = event.dimension();
        editBiome = event.biome();
        editDuration = Integer.toString(event.passiveDurationSeconds());
        editWeight = Double.toString(event.weight());
        editAvoid = Double.toString(event.avoidChance());
        editMobInput = "";
        editMobAmount = "1";
        editMobName = "";
        editMobNbt = "";
        editMobRange = "12";
        editCommandInput = "";
        editCommandDelay = "0";
        selectedAddedMobIndex = -1;
        selectedCommandIndex = -1;
        editMobs.clear();
        editMobs.addAll(event.mobs());
        editCommands.clear();
        editCommands.addAll(event.commands());
    }

    private void captureEditorFields() {
        if (idInput != null) {
            editId = idInput.getValue();
        }
        if (titleInput != null) {
            editTitle = titleInput.getValue();
        }
        if (descriptionInput != null) {
            editDescription = descriptionInput.getValue();
        }
        if (dimensionInput != null) {
            editDimension = dimensionInput.getValue();
        }
        if (biomeInput != null) {
            editBiome = biomeInput.getValue();
        }
        if (durationInput != null) {
            editDuration = durationInput.getValue();
        }
        if (weightInput != null) {
            editWeight = weightInput.getValue();
        }
        if (avoidInput != null) {
            editAvoid = avoidInput.getValue();
        }
        if (mobInput != null) {
            editMobInput = mobInput.getValue();
        }
        if (mobAmountInput != null) {
            editMobAmount = mobAmountInput.getValue();
        }
        if (mobNameInput != null) {
            editMobName = mobNameInput.getValue();
        }
        if (mobNbtInput != null) {
            editMobNbt = mobNbtInput.getValue();
        }
        if (mobRangeInput != null) {
            editMobRange = mobRangeInput.getValue();
        }
        if (commandInput != null) {
            editCommandInput = commandInput.getValue();
        }
        if (commandDelayInput != null) {
            editCommandDelay = commandDelayInput.getValue();
        }
    }

    private void saveEditedEvent() {
        commitCurrentMobEdit(false);
        EventProfile profile = selectedProfile();
        List<EditableTravelEvent> events = new ArrayList<>(profile.events());
        EditableTravelEvent event = new EditableTravelEvent(
                cleanId(idInput.getValue()),
                enabled,
                titleInput.getValue().trim(),
                passive,
                parseInt(durationInput.getValue(), 0),
                descriptionInput.getValue().trim(),
                dimensionInput.getValue().trim(),
                biomeInput.getValue().trim(),
                timeOfDay,
                aggressiveCompletion,
                parseDouble(weightInput.getValue(), 1.0D),
                parseDouble(avoidInput.getValue(), 0.0D),
                List.copyOf(editMobs),
                List.copyOf(editCommands));
        if (eventIndex >= 0 && eventIndex < events.size()) {
            events.set(eventIndex, event);
        } else {
            events.add(event);
            eventIndex = events.size() - 1;
        }
        profiles.set(profileIndex, profile.withEvents(events));
        saveSelectedProfile();
        view = View.EVENTS;
        rebuild();
    }

    private void addSelectedMob() {
        captureEditorFields();
        if (commitCurrentMobEdit(true)) {
            return;
        }
        if (!mobs.isEmpty()) {
            editMobs.add(mobEntry(mobs.get(Math.max(0, Math.min(selectedMobIndex, mobs.size() - 1))), parseInt(mobAmountInput == null ? "1" : mobAmountInput.getValue(), 1), parseInt(mobRangeInput == null ? "12" : mobRangeInput.getValue(), 12), mobNameInput == null ? "" : mobNameInput.getValue().trim(), normalizeNbt(mobNbtInput == null ? "" : mobNbtInput.getValue().trim())));
            clearMobSelection();
        }
    }

    private boolean commitCurrentMobEdit(boolean clearAfter) {
        String typed = mobInput == null ? "" : mobInput.getValue().trim();
        if (typed.isBlank()) {
            return false;
        }
        editMobInput = typed;
        editMobAmount = mobAmountInput == null ? "1" : mobAmountInput.getValue();
        editMobRange = mobRangeInput == null ? "12" : mobRangeInput.getValue();
        editMobName = mobNameInput == null ? "" : mobNameInput.getValue();
        editMobNbt = mobNbtInput == null ? "" : mobNbtInput.getValue();
        String entry = mobEntry(
                typed,
                parseInt(editMobAmount, 1),
                parseInt(editMobRange, 12),
                editMobName.trim(),
                normalizeNbt(editMobNbt.trim()));
        if (selectedAddedMobIndex >= 0 && selectedAddedMobIndex < editMobs.size()) {
            editMobs.set(selectedAddedMobIndex, entry);
        } else {
            editMobs.add(entry);
        }
        if (clearAfter) {
            clearMobSelection();
        }
        return true;
    }

    private void removeSelectedMob() {
        if (selectedAddedMobIndex >= 0 && selectedAddedMobIndex < editMobs.size()) {
            editMobs.remove(selectedAddedMobIndex);
            clearMobSelection();
        }
    }

    private void clearMobSelection() {
        captureEditorFields();
        selectedAddedMobIndex = -1;
        editMobInput = "";
        editMobAmount = "1";
        editMobName = "";
        editMobNbt = "";
        editMobRange = "12";
        if (mobInput != null) {
            mobInput.setValue("");
        }
        if (mobAmountInput != null) {
            mobAmountInput.setValue("1");
        }
        if (mobNameInput != null) {
            mobNameInput.setValue("");
        }
        if (mobNbtInput != null) {
            mobNbtInput.setValue("");
        }
        if (mobRangeInput != null) {
            mobRangeInput.setValue("12");
        }
        rebuild();
    }

    private void addCommand() {
        captureEditorFields();
        if (!commandInput.getValue().trim().isBlank()) {
            editCommandInput = commandInput.getValue().trim();
            editCommandDelay = commandDelayInput.getValue().trim();
            EventCommandStep step = new EventCommandStep(editCommandInput, parseInt(editCommandDelay, 0));
            if (selectedCommandIndex >= 0 && selectedCommandIndex < editCommands.size()) {
                editCommands.set(selectedCommandIndex, step);
            } else {
                editCommands.add(step);
            }
            clearCommandSelection();
        }
    }

    private void removeSelectedCommand() {
        if (selectedCommandIndex >= 0 && selectedCommandIndex < editCommands.size()) {
            editCommands.remove(selectedCommandIndex);
            clearCommandSelection();
        }
    }

    private void clearCommandSelection() {
        captureEditorFields();
        selectedCommandIndex = -1;
        editCommandInput = "";
        editCommandDelay = "0";
        if (commandInput != null) {
            commandInput.setValue("");
        }
        if (commandDelayInput != null) {
            commandDelayInput.setValue("0");
        }
        rebuild();
    }

    private void moveSelectedCommand(int direction) {
        if (selectedCommandIndex < 0 || selectedCommandIndex >= editCommands.size()) {
            return;
        }
        int target = selectedCommandIndex + direction;
        if (target < 0 || target >= editCommands.size()) {
            return;
        }
        EventCommandStep step = editCommands.remove(selectedCommandIndex);
        editCommands.add(target, step);
        selectedCommandIndex = target;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xDD15100A);
        graphics.drawCenteredString(font, titleForView(), width / 2, 10, 0xFFE8C878);
        if (helpOpen) {
            for (net.minecraft.client.gui.components.Renderable renderable : renderables) {
                if (renderable instanceof net.minecraft.client.gui.components.AbstractWidget widget && widget.getY() < 55) {
                    renderable.render(graphics, mouseX, mouseY, partialTick);
                }
            }
            renderHelpOverlay(graphics);
            return;
        }
        if (view == View.PROFILES) {
            renderProfiles(graphics, mouseX, mouseY);
        } else if (view == View.EVENTS) {
            renderEvents(graphics, mouseX, mouseY);
        } else {
            renderEventEditor(graphics, mouseX, mouseY);
        }
        for (net.minecraft.client.gui.components.Renderable renderable : renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
        if (mobDropdownOpen) {
            renderMobDropdown(graphics, mouseX, mouseY);
        }
        if (dimensionDropdownOpen) {
            renderStringDropdown(graphics, editorLeft(), 176, 260, filteredDimensions(), dimensionDropdownScroll);
        }
        if (biomeDropdownOpen && dimensionInput != null && !dimensionInput.getValue().trim().isBlank()) {
            renderStringDropdown(graphics, editorLeft() + 320, 176, 260, filteredBiomes(), biomeDropdownScroll);
        }
    }

    private Component titleForView() {
        if (view == View.EVENTS && !profiles.isEmpty()) {
            return Component.literal("Events - " + selectedProfile().name());
        }
        if (view == View.EDIT_EVENT) {
            return Component.literal("Create / Edit Event");
        }
        return title;
    }

    private void renderProfiles(GuiGraphics graphics, int mouseX, int mouseY) {
        int left = width / 2 - 150;
        int top = 58;
        for (int i = scroll; i < profiles.size() && i < scroll + visibleRows(top); i++) {
            int y = top + (i - scroll) * 22;
            int color = i == profileIndex ? 0xFF8F6A38 : 0xFF3B2A18;
            graphics.fill(left, y, left + 300, y + 20, color);
            EventProfile profile = profiles.get(i);
            graphics.drawString(font, profile.name() + "  [" + profile.id() + "]  " + profile.events().size() + " events", left + 5, y + 6, 0xFFFFE6B0, false);
        }
    }

    private void renderEvents(GuiGraphics graphics, int mouseX, int mouseY) {
        if (profiles.isEmpty()) {
            return;
        }
        int left = Math.max(18, width / 2 - 370);
        int rowWidth = Math.min(width - 36, 740);
        int top = 76;
        graphics.drawString(font, "Name", left + 5, top - 12, 0xFFE8C878, false);
        graphics.drawString(font, "Status", left + 230, top - 12, 0xFFE8C878, false);
        graphics.drawString(font, "Biome", left + 315, top - 12, 0xFFE8C878, false);
        graphics.drawString(font, "Dimension", left + 510, top - 12, 0xFFE8C878, false);
        graphics.drawString(font, "Delete", left + rowWidth - 70, top - 12, 0xFFE8C878, false);
        List<EditableTravelEvent> events = selectedProfile().events();
        for (int i = scroll; i < events.size() && i < scroll + visibleRows(top); i++) {
            int y = top + (i - scroll) * 22;
            int color = i == eventIndex ? 0xFF8F6A38 : 0xFF3B2A18;
            graphics.fill(left, y, left + rowWidth, y + 20, color);
            EditableTravelEvent event = events.get(i);
            graphics.drawString(font, ellipsize(event.title(), 210), left + 5, y + 6, 0xFFFFE6B0, false);
            graphics.drawString(font, event.enabled() ? "On" : "Off", left + 230, y + 6, event.enabled() ? 0xFF88FF88 : 0xFFFF8888, false);
            graphics.drawString(font, ellipsize(event.biome().isBlank() ? "Global" : event.biome(), 180), left + 315, y + 6, 0xFFFFE6B0, false);
            graphics.drawString(font, ellipsize(event.dimension().isBlank() ? "Global" : event.dimension(), 145), left + 510, y + 6, 0xFFFFE6B0, false);
            graphics.fill(left + rowWidth - 70, y + 2, left + rowWidth - 6, y + 18, 0xFF5A2020);
            graphics.drawString(font, "Delete", left + rowWidth - 58, y + 6, 0xFFFFB0B0, false);
        }
    }

    private void renderEventEditor(GuiGraphics graphics, int mouseX, int mouseY) {
        int left = editorLeft();
        drawLabel(graphics, "Event ID", left, 64);
        drawLabel(graphics, "Title shown to players", left + 210, 64);
        drawLabel(graphics, "Subtitle shown on travel screen", left, 102);
        drawLabel(graphics, "Dimension ID (blank = global)", left, 140);
        drawLabel(graphics, "Biome ID (blank = global)", left + 310, 140);
        drawLabel(graphics, "Duration seconds", left, 178);
        drawLabel(graphics, "Selection weight", left + 130, 178);
        drawLabel(graphics, "Avoid chance 0-1", left + 260, 178);
        drawLabel(graphics, "Mob ID to spawn", left, 220);
        drawLabel(graphics, "Amount", left + 310, 220);
        drawLabel(graphics, "Spawn Range", left + 368, 220);
        drawLabel(graphics, "Mob display name", left, 254);
        drawLabel(graphics, "Mob NBT data", left + 230, 254);

        int mobsTop = mobsPanelTop();
        int mobPanelHeight = mobPanelHeight();
        graphics.fill(left, mobsTop, left + 626, mobsTop + mobPanelHeight, 0xAA24190F);
        graphics.drawString(font, "Mobs added to this event", left + 6, mobsTop + 5, 0xFFE8C878, false);
        int visibleMobRows = Math.max(1, (mobPanelHeight - 18) / 11);
        for (int row = 0; row < Math.min(visibleMobRows, editMobs.size() - addedMobScroll); row++) {
            int i = addedMobScroll + row;
            int rowY = mobsTop + 18 + row * 11;
            if (i == selectedAddedMobIndex) {
                graphics.fill(left + 8, rowY - 2, left + 618, rowY + 10, 0xFF8F6A38);
            }
            graphics.drawString(font, "- " + mobDisplay(editMobs.get(i)), left + 12, rowY, 0xFFFFE6B0, false);
        }
        if (editMobs.isEmpty()) {
            graphics.drawString(font, "No mobs added yet.", left + 12, mobsTop + 20, 0xFFBFAE86, false);
        }

        int commandY = commandInputY();
        int cmdY = commandListY();
        int cmdHeight = commandListHeight();
        int commandLeft = commandLeft();
        int commandWidth = commandPanelWidth();
        drawLabel(graphics, "Command", commandLeft, commandY - 12);
        drawLabel(graphics, "Delay", commandLeft, commandY + 24);
        graphics.fill(commandLeft, cmdY, commandLeft + commandWidth, cmdY + cmdHeight, 0xAA24190F);
        graphics.drawString(font, "Command list", commandLeft + 6, cmdY + 5, 0xFFE8C878, false);
        int visibleCommandRows = Math.max(1, (cmdHeight - 18) / 14);
        for (int row = 0; row < Math.min(visibleCommandRows, editCommands.size() - commandListScroll); row++) {
            int i = commandListScroll + row;
            int rowY = cmdY + 18 + row * 14;
            if (i == selectedCommandIndex) {
                graphics.fill(commandLeft + 8, rowY - 2, commandLeft + commandWidth - 8, rowY + 11, 0xFF8F6A38);
            }
            EventCommandStep step = editCommands.get(i);
            graphics.drawString(font, ellipsize("delay " + step.delaySeconds() + "s  |  " + step.command(), commandWidth - 56), commandLeft + 12, rowY, 0xFFFFE6B0, false);
        }
        if (editCommands.isEmpty()) {
            graphics.drawString(font, "No commands added yet.", commandLeft + 12, cmdY + 22, 0xFFBFAE86, false);
        }

        renderInlineCommandSuggestions(graphics, commandLeft, commandY + 52);

    }

    private void drawLabel(GuiGraphics graphics, String text, int x, int y) {
        graphics.drawString(font, text, x, y, 0xFFE8C878, false);
    }

    private void renderHelpOverlay(GuiGraphics graphics) {
        int panelX = 20;
        int panelY = 54;
        int panelW = width - 40;
        int panelH = height - 92;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 1000.0F);
        graphics.fill(0, panelY - 6, width, height, 0xFF15100A);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF1F160D);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 48, 0xFF5A3A1E);
        graphics.drawString(font, "KMD Event Editor Help", panelX + 8, panelY + 6, 0xFFFFE6B0, false);
        String[] tabs = helpTabs();
        int tabX = panelX + 8;
        int tabY = panelY + 24;
        for (int i = 0; i < tabs.length; i++) {
            int tabW = Math.max(58, font.width(tabs[i]) + 14);
            if (tabX + tabW > panelX + panelW - 8) {
                tabX = panelX + 8;
                tabY += 16;
            }
            int color = i == helpPage ? 0xFF8F6A38 : 0xFF2C1D10;
            graphics.fill(tabX, tabY, tabX + tabW, tabY + 14, color);
            graphics.drawString(font, tabs[i], tabX + 7, tabY + 4, 0xFFFFE6B0, false);
            tabX += tabW + 4;
        }
        int x = panelX + 14;
        int y = panelY + 58;
        int w = panelW - 28;
        if (helpPage == 0) {
            y = helpSection(graphics, "Profiles", "Profiles are complete event sets. A server can keep several profiles, such as default, peaceful_roads, hard_mode, quest_ambushes, or modded_events.", x, y, w);
            y = helpSection(graphics, "Default profile", "The default profile is used when a player has no personal override. If you delete every profile, KMD has no custom events to choose from until you create or import one.", x, y, w);
            y = helpSection(graphics, "Saving", "Profiles are saved into the world data and mirrored to JSON under kmdtravel/events/<world-name>. In-game edits update that world's JSON. Deleting a profile in-game deletes its JSON file too.", x, y, w);
            helpExample(graphics, "Workflow", "Create Profile -> open it -> Create Event -> Save Event -> test by fast travelling.", x, y, w);
        } else if (helpPage == 1) {
            y = helpSection(graphics, "Events", "An event is one possible interruption during fast travel. Enabled events from the player\'s active profile enter the pool if their dimension, biome, and time rules match the travel route.", x, y, w);
            y = helpSection(graphics, "Passive vs aggressive", "Passive events show a timer and then travel continues. Aggressive events can either wait for spawned mobs to die or resume after a timer depending on Ends: Kill / Ends: Timed.", x, y, w);
            y = helpSection(graphics, "Selection weight", "Weight controls how often this event is chosen compared with other matching events. Weight 2.0 is twice as likely as weight 1.0. Weight 0 effectively prevents selection.", x, y, w);
            helpExample(graphics, "Example", "Road Bandit: aggressive, Ends: Kill, overworld, biome blank, weight 1.0, avoid chance 0.15.", x, y, w);
        } else if (helpPage == 2) {
            y = helpSection(graphics, "Main fields", "Event ID is the saved key, for example road_bandits. Title is the big event text. Subtitle is the smaller line shown below it on the travel screen.", x, y, w);
            y = helpSection(graphics, "Dimension and biome", "Blank dimension means any dimension. Blank biome means any biome. Select or type a dimension first, then choose a biome. Modded dimensions and biomes can be typed manually.", x, y, w);
            y = helpSection(graphics, "Duration", "For passive events, duration is the countdown time. For aggressive timed events, duration is how long until travel resumes. Aggressive kill events ignore duration for completion.", x, y, w);
            helpExample(graphics, "Examples", "Dimension: minecraft:overworld | minecraft:the_nether | minecraft:the_end. Biome: minecraft:beach | minecraft:desert | modid:custom_biome.", x, y, w);
        } else if (helpPage == 3) {
            y = helpSection(graphics, "Mobs", "Mob ID is the entity registry id. Vanilla examples: minecraft:zombie, minecraft:skeleton, minecraft:pillager, minecraft:drowned. Modded mobs use the mod id, for example modid:troll.", x, y, w);
            y = helpSection(graphics, "Amount and range", "Amount is exact number spawned from this row. Spawn Range controls how far from the traveler KMD tries to place them, so players get a chance to react.", x, y, w);
            y = helpSection(graphics, "Mob display name", "Optional. This sets the name above the mob. Leave it blank if you want the normal Minecraft mob name.", x, y, w);
            helpExample(graphics, "Example row", "Mob ID: minecraft:skeleton | Amount: 3 | Spawn Range: 18 | Name: Road Archer | NBT: PersistenceRequired:1b", x, y, w);
        } else if (helpPage == 4) {
            y = helpSection(graphics, "NBT / SNBT", "NBT uses Minecraft SNBT entity data. In this editor you can write either a full compound with braces or just the inside. Both NoAI:1b and {NoAI:1b} are accepted.", x, y, w);
            y = helpSection(graphics, "Common tags", "NoAI:1b disables AI. PersistenceRequired:1b keeps the mob from despawning. Health:40f sets health. CustomName sets a JSON text name.", x, y, w);
            y = helpSection(graphics, "1.21 item syntax", "Item stacks use lowercase count. ArmorItems order is boots, leggings, chestplate, helmet. HandItems order is main hand, off hand.", x, y, w);
            helpExample(graphics, "Name + sword", "CustomName:'{\"text\":\"Bandit Captain\",\"color\":\"gold\"}',PersistenceRequired:1b,HandItems:[{id:\"minecraft:iron_sword\",count:1b},{id:\"minecraft:air\",count:0b}]", x, y, w);
        } else if (helpPage == 5) {
            y = helpSection(graphics, "Commands", "Commands run when the event begins. Each command row has its own delay. Use Up and Down to control order. Do not start commands with a slash.", x, y, w);
            y = helpSection(graphics, "Safe targeting", "Use {player} instead of @p. In multiplayer, @p can pick the wrong nearby player. {player} is replaced with the traveler who triggered the event.", x, y, w);
            y = helpSection(graphics, "Sound example", "playsound minecraft:entity.ghast.scream hostile {player} {x} {y} {z} 1 0.4", x, y, w);
            helpExample(graphics, "Useful commands", "effect give {player} minecraft:slowness 10 1 | title {player} actionbar {\"text\":\"Ambush!\"} | give {player} minecraft:bread 2", x, y, w);
        } else if (helpPage == 6) {
            y = helpSection(graphics, "Placeholders", "KMD replaces placeholders before running a command. {player} is the traveler name. {uuid} is their unique id. {x} {y} {z} are the event spawn location. {event_tag} is unique to that one event instance.", x, y, w);
            y = helpSection(graphics, "Event tags", "Use {event_tag} when command-spawned mobs must be tracked by Ends: Kill. Built-in mob rows are tracked automatically, but command-spawned mobs need the tag.", x, y, w);
            y = helpSection(graphics, "Why it matters", "If two players trigger events at the same time, unique event tags prevent Player A\'s mobs from completing Player B\'s event.", x, y, w);
            helpExample(graphics, "Tracked summon", "summon minecraft:zombie {x} {y} {z} {Tags:[\"{event_tag}\"],CustomName:'{\"text\":\"Tagged Bandit\"}' }", x, y, w);
        } else {
            y = helpSection(graphics, "Quick recipes", "These are copy-friendly examples you can paste and adjust. Remember: commands do not use a leading slash, and command-spawned kill mobs need Tags:[\"{event_tag}\"].", x, y, w);
            y = helpSection(graphics, "Basic bandit", "summon minecraft:zombie {x} {y} {z} {Tags:[\"{event_tag}\"],CustomName:'{\"text\":\"Road Bandit\",\"color\":\"gold\"}',PersistenceRequired:1b,HandItems:[{id:\"minecraft:iron_sword\",count:1b},{id:\"minecraft:air\",count:0b}]}", x, y, w);
            y = helpSection(graphics, "Passive flavor", "playsound minecraft:block.campfire.crackle ambient {player} {x} {y} {z} 1 1", x, y, w);
            helpExample(graphics, "No AI statue mob", "Mob NBT: NoAI:1b,PersistenceRequired:1b,CustomName:'{\"text\":\"Frozen Guard\",\"color\":\"gray\"}'", x, y, w);
        }
        graphics.pose().popPose();
    }

    private static String[] helpTabs() {
        return new String[]{"Profiles", "Events", "Fields", "Mobs", "NBT", "Commands", "Tags", "Examples"};
    }

    private int helpSection(GuiGraphics graphics, String header, String text, int x, int y, int maxWidth) {
        graphics.drawString(font, header, x, y, 0xFFFFD36A, false);
        drawWrapped(graphics, text, x + 10, y + 13, maxWidth - 10, 0xFFFFE6B0);
        return y + 13 + wrappedHeight(text, maxWidth - 10) + 12;
    }

    private void helpExample(GuiGraphics graphics, String header, String text, int x, int y, int maxWidth) {
        graphics.drawString(font, header, x, y, 0xFFFFD36A, false);
        graphics.fill(x + 10, y + 13, x + maxWidth - 8, y + 17 + wrappedHeight(text, maxWidth - 26), 0xAA090603);
        drawWrapped(graphics, text, x + 16, y + 17, maxWidth - 32, 0xFFBFEFFF);
    }

    private int wrappedHeight(String text, int maxWidth) {
        int lines = 1;
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            while (font.width(word) > maxWidth && word.length() > 4) {
                if (!line.isEmpty()) {
                    line = new StringBuilder();
                    lines++;
                }
                int cut = Math.max(4, Math.min(word.length(), word.length() * maxWidth / Math.max(1, font.width(word))));
                word = word.substring(cut);
                lines++;
            }
            String next = line.isEmpty() ? word : line + " " + word;
            if (font.width(next) > maxWidth && !line.isEmpty()) {
                line = new StringBuilder(word);
                lines++;
            } else {
                line = new StringBuilder(next);
            }
        }
        return lines * 11;
    }
    private void renderMobDropdown(GuiGraphics graphics, int mouseX, int mouseY) {
        int left = editorLeft();
        int top = 254;
        List<String> options = filteredMobs();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 500.0F);
        graphics.fill(left - 2, top - 2, left + 412, top + 124, 0xFF090603);
        graphics.fill(left, top, left + 410, top + 122, 0xFF24190F);
        int count = Math.min(8, options.size() - mobDropdownScroll);
        for (int i = 0; i < count; i++) {
            String mob = options.get(mobDropdownScroll + i);
            int y = top + 4 + i * 14;
            if (mob.equals(mobInput == null ? "" : mobInput.getValue())) {
                graphics.fill(left + 2, y - 2, left + 408, y + 12, 0xFF8F6A38);
            }
            graphics.drawString(font, mob, left + 6, y, 0xFFFFE6B0, false);
        }
        graphics.pose().popPose();
    }

    private void renderCommandSuggestions(GuiGraphics graphics, int x, int y) {
        String[] suggestions = {
                "summon minecraft:zombie {x} {y} {z} {Tags:[\"{event_tag}\"]}",
                "effect give {player} minecraft:slowness 10 1",
                "playsound minecraft:entity.witch.celebrate player {player}",
                "title {player} actionbar {\"text\":\"Ambush!\"}"
        };
        int row = 0;
        for (String suggestion : suggestions) {
            drawWrapped(graphics, suggestion, x, y + row * 28, 270, 0xFFFFE6B0);
            row++;
        }
        if (row == 0) {
            graphics.drawString(font, "No matching helper yet.", x, y, 0xFFBFAE86, false);
        }
    }

    private void renderStringDropdown(GuiGraphics graphics, int left, int top, int dropdownWidth, List<String> options, int dropdownScroll) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 500.0F);
        graphics.fill(left - 2, top - 2, left + dropdownWidth + 2, top + 124, 0xFF090603);
        graphics.fill(left, top, left + dropdownWidth, top + 122, 0xFF24190F);
        int count = Math.min(8, options.size() - dropdownScroll);
        for (int i = 0; i < count; i++) {
            String value = options.get(dropdownScroll + i);
            graphics.drawString(font, value, left + 6, top + 4 + i * 14, 0xFFFFE6B0, false);
        }
        if (options.isEmpty()) {
            graphics.drawString(font, "Select dimension first", left + 6, top + 6, 0xFFBFAE86, false);
        }
        graphics.pose().popPose();
    }

    private void renderInlineCommandSuggestions(GuiGraphics graphics, int x, int y) {
        if (commandInput == null) {
            return;
        }
        if (!commandInput.isFocused()) {
            return;
        }
        String typed = commandInput.getValue().trim().toLowerCase();
        String[] suggestions = {
                "summon minecraft:zombie {x} {y} {z} {Tags:[\"{event_tag}\"]}",
                "effect give {player} minecraft:slowness 10 1",
                "playsound minecraft:entity.witch.celebrate player {player}",
                "title {player} actionbar {\"text\":\"Ambush!\"}"
        };
        int row = 0;
        for (String suggestion : suggestions) {
            if (!typed.isBlank() && !suggestion.toLowerCase().contains(typed) && !suggestion.toLowerCase().startsWith(typed)) {
                continue;
            }
            graphics.drawString(font, ellipsize(suggestion, 620), x + 6, y + row * 11, 0x66FFE6B0, false);
            row++;
            if (row >= 4) {
                break;
            }
        }
    }

    private void drawWrapped(GuiGraphics graphics, String text, int x, int y, int maxWidth, int color) {
        int lineY = y;
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            while (font.width(word) > maxWidth && word.length() > 4) {
                if (!line.isEmpty()) {
                    graphics.drawString(font, line.toString(), x, lineY, color, false);
                    line = new StringBuilder();
                    lineY += 11;
                }
                int cut = Math.max(4, Math.min(word.length(), word.length() * maxWidth / Math.max(1, font.width(word))));
                graphics.drawString(font, word.substring(0, cut), x, lineY, color, false);
                word = word.substring(cut);
                lineY += 11;
            }
            String next = line.isEmpty() ? word : line + " " + word;
            if (font.width(next) > maxWidth && !line.isEmpty()) {
                graphics.drawString(font, line.toString(), x, lineY, color, false);
                line = new StringBuilder(word);
                lineY += 11;
            } else {
                line = new StringBuilder(next);
            }
        }
        if (!line.isEmpty()) {
            graphics.drawString(font, line.toString(), x, lineY, color, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (helpOpen) {
            if (mouseY < 54) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            int panelX = 20;
            int panelY = 54;
            int panelW = width - 40;
            int tabX = panelX + 8;
            int tabY = panelY + 24;
            String[] tabs = helpTabs();
            for (int i = 0; i < tabs.length; i++) {
                int tabW = Math.max(58, font.width(tabs[i]) + 14);
                if (tabX + tabW > panelX + panelW - 8) {
                    tabX = panelX + 8;
                    tabY += 16;
                }
                if (mouseX >= tabX && mouseX <= tabX + tabW && mouseY >= tabY && mouseY <= tabY + 14) {
                    helpPage = i;
                    return true;
                }
                tabX += tabW + 4;
            }
            return true;
        }
        if (dimensionDropdownOpen && view == View.EDIT_EVENT) {
            int left = editorLeft();
            int top = 176;
            List<String> options = filteredDimensions();
            if (mouseX >= left && mouseX <= left + 260 && mouseY >= top && mouseY <= top + 122) {
                int index = dimensionDropdownScroll + (((int) mouseY - top - 4) / 14);
                if (index >= 0 && index < options.size()) {
                    dimensionInput.setValue(options.get(index));
                    biomeInput.setValue("");
                    dimensionDropdownOpen = false;
                    return true;
                }
            }
        }
        if (biomeDropdownOpen && view == View.EDIT_EVENT && dimensionInput != null && !dimensionInput.getValue().trim().isBlank()) {
            int left = editorLeft() + 320;
            int top = 176;
            List<String> options = filteredBiomes();
            if (mouseX >= left && mouseX <= left + 260 && mouseY >= top && mouseY <= top + 122) {
                int index = biomeDropdownScroll + (((int) mouseY - top - 4) / 14);
                if (index >= 0 && index < options.size()) {
                    biomeInput.setValue(options.get(index));
                    biomeDropdownOpen = false;
                    return true;
                }
            }
        }
        if (mobDropdownOpen && view == View.EDIT_EVENT) {
            int left = editorLeft();
            int top = 254;
            List<String> options = filteredMobs();
            if (mouseX >= left && mouseX <= left + 410 && mouseY >= top && mouseY <= top + 122) {
                int row = ((int) mouseY - top - 4) / 14;
                int mobIndex = mobDropdownScroll + row;
                if (mobIndex >= 0 && mobIndex < options.size()) {
                    selectedMobIndex = mobIndex;
                    mobInput.setValue(options.get(mobIndex));
                    editMobInput = options.get(mobIndex);
                    mobDropdownOpen = false;
                    return true;
                }
            }
        }
        if (view == View.EDIT_EVENT) {
            int left = editorLeft();
            int mobsTop = mobsPanelTop();
            if (mouseX >= left && mouseX <= left + 626 && mouseY >= mobsTop + 18 && mouseY <= mobsTop + mobPanelHeight()) {
                int clicked = addedMobScroll + (((int) mouseY - mobsTop - 18) / 11);
                if (clicked >= 0 && clicked < editMobs.size()) {
                    selectedAddedMobIndex = clicked;
                    editMobInput = mobId(editMobs.get(clicked));
                    editMobAmount = Integer.toString(mobCount(editMobs.get(clicked)));
                    editMobRange = Integer.toString(mobRange(editMobs.get(clicked)));
                    editMobName = mobName(editMobs.get(clicked));
                    editMobNbt = mobNbt(editMobs.get(clicked));
                    rebuild();
                    return true;
                }
            }
            int cmdY = commandListY();
            int commandLeft = commandLeft();
            if (mouseX >= commandLeft && mouseX <= commandLeft + commandPanelWidth() && mouseY >= cmdY + 18 && mouseY <= cmdY + commandListHeight()) {
                int clicked = commandListScroll + (((int) mouseY - cmdY - 18) / 14);
                if (clicked >= 0 && clicked < editCommands.size()) {
                    selectedCommandIndex = clicked;
                    EventCommandStep step = editCommands.get(clicked);
                    editCommandInput = step.command();
                    editCommandDelay = Integer.toString(step.delaySeconds());
                    rebuild();
                    return true;
                }
            }
        }
        if (view == View.PROFILES) {
            int clicked = clickedRow(width / 2 - 150, 58, 300, mouseX, mouseY, profiles.size());
            if (clicked >= 0) {
                profileIndex = clicked;
                profileNameInput.setValue(profiles.get(clicked).name());
                if (lastProfileIndex == clicked && UtilMillis.now() - lastProfileClick < 350) {
                    openSelectedProfile();
                }
                lastProfileIndex = clicked;
                lastProfileClick = UtilMillis.now();
                return true;
            }
        } else if (view == View.EVENTS) {
            int left = Math.max(18, width / 2 - 370);
            int rowWidth = Math.min(width - 36, 740);
            int clicked = clickedRow(left, 76, rowWidth, mouseX, mouseY, selectedProfile().events().size());
            if (clicked >= 0) {
                eventIndex = clicked;
                int rowY = 76 + (clicked - scroll) * 22;
                if (mouseX >= left + rowWidth - 70 && mouseX <= left + rowWidth - 6 && mouseY >= rowY + 2 && mouseY <= rowY + 18) {
                    deleteSelectedEvent();
                    return true;
                }
                if (lastEventIndex == clicked && UtilMillis.now() - lastEventClick < 350) {
                    editSelectedEvent();
                }
                lastEventIndex = clicked;
                lastEventClick = UtilMillis.now();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (dimensionDropdownOpen) {
            int size = filteredDimensions().size();
            dimensionDropdownScroll = Math.max(0, Math.min(Math.max(0, size - 8), dimensionDropdownScroll - (int) Math.signum(scrollY)));
            return true;
        }
        if (biomeDropdownOpen) {
            int size = filteredBiomes().size();
            biomeDropdownScroll = Math.max(0, Math.min(Math.max(0, size - 8), biomeDropdownScroll - (int) Math.signum(scrollY)));
            return true;
        }
        if (mobDropdownOpen) {
            int size = filteredMobs().size();
            mobDropdownScroll = Math.max(0, Math.min(Math.max(0, size - 8), mobDropdownScroll - (int) Math.signum(scrollY)));
            return true;
        }
        if (view == View.EDIT_EVENT) {
            int left = editorLeft();
            int mobsTop = mobsPanelTop();
            int visibleMobRows = Math.max(1, (mobPanelHeight() - 18) / 11);
            if (mouseX >= left && mouseX <= left + 626 && mouseY >= mobsTop && mouseY <= mobsTop + mobPanelHeight()) {
                addedMobScroll = Math.max(0, Math.min(Math.max(0, editMobs.size() - visibleMobRows), addedMobScroll - (int) Math.signum(scrollY)));
                return true;
            }
            int cmdY = commandListY();
            int visibleCommandRows = Math.max(1, (commandListHeight() - 18) / 14);
            int commandLeft = commandLeft();
            if (mouseX >= commandLeft && mouseX <= commandLeft + commandPanelWidth() && mouseY >= cmdY && mouseY <= cmdY + commandListHeight()) {
                commandListScroll = Math.max(0, Math.min(Math.max(0, editCommands.size() - visibleCommandRows), commandListScroll - (int) Math.signum(scrollY)));
                return true;
            }
        }
        int max = view == View.PROFILES ? profiles.size() : view == View.EVENTS && !profiles.isEmpty() ? selectedProfile().events().size() : 0;
        scroll = Math.max(0, Math.min(Math.max(0, max - 8), scroll - (int) Math.signum(scrollY)));
        return true;
    }

    private int clickedRow(int left, int top, int width, double mouseX, double mouseY, int size) {
        if (mouseX < left || mouseX > left + width || mouseY < top) {
            return -1;
        }
        int row = ((int) mouseY - top) / 22;
        int index = scroll + row;
        return index >= 0 && index < size ? index : -1;
    }

    private int visibleRows(int top) {
        return Math.max(1, (height - top - 52) / 22);
    }

    private String completionLabel() {
        return aggressiveCompletion == AggressiveCompletion.KILL_MOBS ? "Kill" : "Timed";
    }

    private String selectedMobLabel() {
        return mobs.isEmpty() ? "No mobs registered" : "Mob: " + mobs.get(Math.max(0, Math.min(selectedMobIndex, mobs.size() - 1)));
    }

    private List<String> filteredMobs() {
        String query = mobInput == null ? editMobInput : mobInput.getValue().trim().toLowerCase();
        if (query.isBlank()) {
            return mobs;
        }
        return mobs.stream()
                .filter(mob -> mob.toLowerCase().contains(query))
                .limit(80)
                .toList();
    }

    private List<String> filteredDimensions() {
        String query = dimensionInput == null ? editDimension : dimensionInput.getValue().trim().toLowerCase();
        if (query.isBlank()) {
            return dimensions;
        }
        return dimensions.stream().filter(value -> value.toLowerCase().contains(query)).limit(80).toList();
    }

    private List<String> filteredBiomes() {
        if (dimensionInput == null || dimensionInput.getValue().trim().isBlank()) {
            return List.of();
        }
        String query = biomeInput == null ? editBiome : biomeInput.getValue().trim().toLowerCase();
        String dimension = dimensionInput.getValue().trim();
        return biomes.stream()
                .filter(value -> biomeMatchesDimension(value, dimension))
                .filter(value -> query.isBlank() || value.toLowerCase().contains(query))
                .limit(80)
                .toList();
    }

    private static boolean biomeMatchesDimension(String biome, String dimension) {
        if ("minecraft:the_nether".equals(dimension)) {
            return biome.contains("nether") || biome.contains("basalt") || biome.contains("crimson") || biome.contains("warped") || biome.contains("soul_sand");
        }
        if ("minecraft:the_end".equals(dimension)) {
            return biome.contains("end") || biome.contains("void");
        }
        if ("minecraft:overworld".equals(dimension)) {
            return biome.startsWith("minecraft:")
                    && !(biome.contains("nether") || biome.contains("basalt") || biome.contains("crimson") || biome.contains("warped") || biome.contains("soul_sand") || biome.contains("end_") || biome.endsWith(":the_end"));
        }
        return true;
    }

    private static String mobEntry(String id, int count, int range, String name, String nbt) {
        return id
                + "|" + Math.max(1, Math.min(64, count))
                + "|" + Math.max(4, Math.min(96, range))
                + "|" + encode(name)
                + "|" + encode(nbt);
    }

    private static String mobId(String entry) {
        return mobParts(entry)[0];
    }

    private static int mobCount(String entry) {
        String[] parts = mobParts(entry);
        return parts.length > 1 ? parseInt(parts[1], 1) : 1;
    }

    private static int mobRange(String entry) {
        String[] parts = mobParts(entry);
        return parts.length > 2 ? parseInt(parts[2], 12) : 12;
    }

    private static String mobName(String entry) {
        String[] parts = mobParts(entry);
        return parts.length > 3 ? decode(parts[3]) : "";
    }

    private static String mobNbt(String entry) {
        String[] parts = mobParts(entry);
        return parts.length > 4 ? decode(parts[4]) : "";
    }

    private static String mobDisplay(String entry) {
        String name = mobName(entry);
        String nbt = mobNbt(entry);
        return mobId(entry) + " x" + mobCount(entry) + " range " + mobRange(entry)
                + (name.isBlank() ? "" : " named " + name)
                + (nbt.isBlank() ? "" : " nbt");
    }

    private static String normalizeNbt(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("{\"text\"") || trimmed.startsWith("{text")) {
            int namedJsonEnd = trimmed.indexOf("}',");
            if (namedJsonEnd >= 0) {
                String nameJson = trimmed.substring(0, namedJsonEnd + 1);
                String rest = trimmed.substring(namedJsonEnd + 2);
                return "{CustomName:'" + nameJson + "'," + rest.replace("Count:", "count:") + "}";
            }
            return "{CustomName:'" + trimmed + "'}";
        }
        trimmed = trimmed.replace("Count:", "count:");
        return trimmed.startsWith("{") && trimmed.endsWith("}") ? trimmed : "{" + trimmed + "}";
    }

    private String ellipsize(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String suffix = "...";
        int end = text.length();
        while (end > 0 && font.width(text.substring(0, end) + suffix) > maxWidth) {
            end--;
        }
        return text.substring(0, Math.max(0, end)) + suffix;
    }

    private static String[] mobParts(String entry) {
        return entry.split("\\|", -1);
    }

    private static String encode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignored) {
            return "";
        }
    }

    private static String cleanId(String value) {
        String clean = value.trim().toLowerCase().replaceAll("[^a-z0-9_/-]", "_");
        return clean.isBlank() ? "custom_event" : clean;
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class UtilMillis {
        private static long now() {
            return System.currentTimeMillis();
        }
    }
}


