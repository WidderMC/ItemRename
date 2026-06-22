package widder.itemrename;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class Rename {

    //Regist Command
    public static void RegisterCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
            dispatcher.register(Commands.literal("rename")
                    .then(Commands.argument("text", StringArgumentType.greedyString())
                            .executes(context -> {
                                return rename(context.getSource(), StringArgumentType.getString(context, "text"), false);
                            }))
                    .then(Commands.literal("add")
                            .then(Commands.argument("text", StringArgumentType.greedyString())
                                    .executes(context -> rename(context.getSource(), StringArgumentType.getString(context,"text"), true)))));
        });
    }

    private static int rename(CommandSourceStack source, String text, boolean add) {
        //Test if a Player runs the command
        @Nullable Entity entity = source.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            source.sendSuccess(() -> Component.literal("Command must be executed by a Player"),false);
            return 0;
        }

        //Test if the Player has an Item in the Hand
        ItemStack item = player.getMainHandItem();
        if (item.isEmpty()) {
            source.sendSuccess(() -> Component.literal("You has to hold an item in their Main Hand"),false);
            return 0;
        }

        //Switch between /rename <Text> and /rename add <Text>
        if (add == false) {
            //Apply Style to Text
            Component finaltext = ApplyStyle(text);

            //Rename Item
            item.set(DataComponents.CUSTOM_NAME, finaltext);
        } else if (add == true) {
            try {
                //Apply Style to Text and Merge it with item name
                Component itemText = item.get(DataComponents.CUSTOM_NAME);
                Component newText = ApplyStyle(text);
                MutableComponent finaltext = itemText.copy().append(newText);

                //Rename Item
                item.set(DataComponents.CUSTOM_NAME, finaltext);
            } catch (Exception e) {
                source.sendSuccess(() -> Component.literal("Do /rename <Text> first"), false);
            }
        }
        return 1;
    }

    private static Component ApplyStyle(String text) {
        MutableComponent finaltext = Component.empty();
        TextColor currentColor = null;
        boolean withItalic = false;
        boolean withObfuscated = false;
        boolean withBold = false;
        boolean withStrikethrough = false;
        boolean withUnderlined = false;

        while (!text.isEmpty()) {

            //Color
            if (text.startsWith("#") && text.length() >= 7) {
                try {
                    currentColor = TextColor.parseColor(text.substring(0, 7)).getOrThrow();
                    text = text.substring(7);
                    continue;
                } catch (Exception e) {}
            }

            //Italic
            if (text.startsWith("<")) {
                int NextOpen = text.indexOf("<",1);
                int NextClose = text.indexOf(">");

                if (NextClose != -1 && (NextOpen == -1 || NextClose < NextOpen)) {
                    text = text.substring(1);
                    withItalic = true;
                    continue;
                }
            } else if (text.startsWith(">") && withItalic == true) {
                text = text.substring(1);
                withItalic = false;
                continue;
            }

            //Obfuscated
            if (text.startsWith("{")) {
                int NextOpen = text.indexOf("{",1);
                int NextClose = text.indexOf("}");

                if (NextClose != -1 && (NextOpen == -1 || NextClose < NextOpen)) {
                    text = text.substring(1);
                    withObfuscated = true;
                    continue;
                }
            } else if (text.startsWith("}") && withObfuscated == true) {
                text = text.substring(1);
                withObfuscated = false;
                continue;
            }

            //Bold
            if (text.startsWith("[")) {
                int NextOpen = text.indexOf("[",1);
                int NextClose = text.indexOf("]");

                if (NextClose != -1 && (NextOpen == -1 || NextClose < NextOpen)) {
                    text = text.substring(1);
                    withBold = true;
                    continue;
                }
            } else if (text.startsWith("]") && withBold == true) {
                text = text.substring(1);
                withBold = false;
                continue;
            }

            //Strikethrough
            if (text.startsWith("(")) {
                int NextOpen = text.indexOf("(",1);
                int NextClose = text.indexOf(")");

                if (NextClose != -1 && (NextOpen == -1 || NextClose < NextOpen)) {
                    text = text.substring(1);
                    withStrikethrough = true;
                    continue;
                }
            } else if (text.startsWith(")") && withStrikethrough == true) {
                text = text.substring(1);
                withStrikethrough = false;
                continue;
            }

            //Underlined
            if (text.startsWith("/")) {
                int NextOpen = text.indexOf("/",1);
                int NextClose = text.indexOf("|");

                if (NextClose != -1 && (NextOpen == -1 || NextClose < NextOpen)) {
                    text = text.substring(1);
                    withUnderlined = true;
                    continue;
                }
            } else if (text.startsWith("|") && withUnderlined == true) {
                text = text.substring(1);
                withUnderlined = false;
                continue;
            }

            //Apply Style
            if (text.isEmpty()) break;
            Style currentstyle = Style.EMPTY
                    .withItalic(withItalic)
                    .withObfuscated(withObfuscated)
                    .withBold(withBold)
                    .withStrikethrough(withStrikethrough)
                    .withUnderlined(withUnderlined)
                    .withColor(currentColor);
            finaltext.append(Component.literal(text.substring(0, 1)).withStyle(currentstyle));
            text = text.substring(1);
        }
        return finaltext;
    }
}