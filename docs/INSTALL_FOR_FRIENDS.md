# Dorohedoro: The Hole — tester guide

Thanks for testing. This is an **early, unfinished fan mod**. The Overworld becomes the Hole, an endless polluted city from the manga Dorohedoro. Right now it is the city, survival and exploration. There are no sorcerers, magic, other dimensions or portals yet.

## What you need

- Minecraft: Java Edition, paid and installed. Bedrock will not work.
- `dorohedoro-0.1.0.jar`, from whoever sent you this guide.
- At least 4 GB of memory to spare.

The official launcher includes the Java that Minecraft needs. If the NeoForge installer will not open, install [Java 21](https://adoptium.net/temurin/releases/?version=21) and try again.

## Install on Windows

1. Run Minecraft once on version 1.21.1 if you never have, then close the launcher.
2. Download [neoforge-21.1.265-installer.jar](https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.265/neoforge-21.1.265-installer.jar). The version must match exactly.
3. Double-click it, choose **Install client**, keep the default folder, and click **OK**.
4. Create a folder for the mod, for example `C:\Games\Dorohedoro`, and a folder called `mods` inside it.
5. Put `dorohedoro-0.1.0.jar` in that `mods` folder.
6. In the Minecraft Launcher, open **Installations**, then **New Installation**:
   - **Name:** `Dorohedoro`
   - **Version:** the entry containing `neoforge-21.1.265`
   - **Game directory:** `C:\Games\Dorohedoro`
   - **More Options, JVM Arguments:** change `-Xmx2G` to `-Xmx4G`
7. Click **Create**, select **Dorohedoro**, and press **Play**. Allow the launcher to play modded if it asks.

The separate folder keeps this away from your normal worlds and other mods.

**Mac or Linux:** the steps are the same, but use a folder in your home directory. Double-click may not open the installer, so run `java -jar neoforge-21.1.265-installer.jar` in a terminal.

## Play alone

Choose Singleplayer, then Create New World. On the World tab, World Type should say **The Hole**. Your existing worlds are not changed, and only new worlds become the Hole.

## Play together

Everyone needs the same mod file and the same NeoForge version, or the game refuses to connect.

**Same home network (easiest).** The host opens their world, presses Esc, and chooses **Open to LAN**. Everyone else opens Multiplayer and it appears in the list, or uses Direct Connect with the address the host's chat shows.

**Over the internet.** The host has three options:

- Run [Tailscale](https://tailscale.com/) on every player's PC, then use Open to LAN and connect to the host's Tailscale address plus the LAN port.
- Forward the port on the host's router, which exposes it to the internet. Only do this if you know how to secure it.
- Rent or run a dedicated server, below.

## Run a dedicated server

1. Make an empty folder and put the NeoForge installer in it.
2. In that folder, run:

```bash
java -jar neoforge-21.1.265-installer.jar --install-server
```

3. Make a `mods` folder and put `dorohedoro-0.1.0.jar` in it.
4. Start it with `run.bat` on Windows or `run.sh` on Mac and Linux. It stops the first time.
5. Read the [Minecraft EULA](https://aka.ms/MinecraftEULA). If you accept it, set `eula=true` in `eula.txt`.
6. Start it again. Do not change `level-type` in `server.properties`, because the default is already the Hole.

## What to test and report

Most useful first:

1. Does the game start, and does the world load?
2. Did any warning screen appear when creating the world?
3. Does the city look right? Screenshots help a lot. Press F2.
4. Can you enter buildings and climb stairs to the roof?
5. Do chests, beds, crafting, furnaces and farming work?
6. Do lag or stutters appear when walking through new areas?
7. Can you go down a street hatch into the sewers and mines?
8. Do villagers and animals behave normally?

## If something breaks

Send these from your Dorohedoro game folder:

- `logs\latest.log`
- The newest file in `crash-reports`, if that folder exists

Also say what you were doing, and share your world seed if you can. Type `/seed` to see it, which needs cheats on.

## Good to know

- The mod is early, so expect bugs. **Do not get attached to a world**, because updates can change the city and leave visible seams.
- Do not repost the mod. It is an unofficial fan project, and Dorohedoro belongs to Q Hayashida.
