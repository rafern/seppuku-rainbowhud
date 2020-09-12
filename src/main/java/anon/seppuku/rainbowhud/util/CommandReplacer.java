package me.anon.seppuku.rainbowhud.util;

import java.util.List;
import java.util.Iterator;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.impl.management.CommandManager;

public final class CommandReplacer {
    private CommandReplacer() {} // "static"

    // Replaces a command of a given type with an instance of a given command
    public static void replace(Class<?> removeClass, Command replacement) {
        // Get commands
        CommandManager comMan = Seppuku.INSTANCE.getCommandManager();
        List<Command> commands = comMan.getCommandList();
        Iterator<Command> cit = commands.iterator();

        // Remove all target instances
        while(cit.hasNext()) {
            Command com = cit.next();
            if(removeClass.isInstance(com)) {
                cit.remove();
            }
        }

        // Add command
        commands.add(replacement);
        comMan.setCommandList(commands);
    }
}
