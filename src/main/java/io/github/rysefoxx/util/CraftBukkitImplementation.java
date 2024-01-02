/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */


package io.github.rysefoxx.util;

import io.github.rysefoxx.manager.GroupMemberManager;
import io.github.rysefoxx.manager.GroupPermissionManager;
import io.github.rysefoxx.permission.LegendPermissibleBase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <a href="https://github.com/LuckPerms/LuckPerms/blob/master/bukkit/src/main/java/me/lucko/luckperms/bukkit/util/CraftBukkitImplementation.java#L33">Source</a>
 */
public class CraftBukkitImplementation {
    private CraftBukkitImplementation() {
    }

    private static final String SERVER_PACKAGE_VERSION;

    static {
        Class<?> server = Bukkit.getServer().getClass();
        Matcher matcher = Pattern.compile("^org\\.bukkit\\.craftbukkit\\.(\\w+)\\.CraftServer$").matcher(server.getName());
        if (matcher.matches()) {
            SERVER_PACKAGE_VERSION = '.' + matcher.group(1) + '.';
        } else {
            SERVER_PACKAGE_VERSION = ".";
        }
    }

    public static String obc(String className) {
        return "org.bukkit.craftbukkit" + SERVER_PACKAGE_VERSION + className;
    }

    public static Class<?> obcClass(String className) throws ClassNotFoundException {
        return Class.forName(obc(className));
    }

    public static void injectEntity(@NotNull Player player, @NotNull GroupPermissionManager groupPermissionManager, @NotNull GroupMemberManager groupMemberManager) throws Exception {
        // get the CraftEntity class
        Class<?> entityClass = CraftBukkitImplementation.obcClass("entity.CraftEntity");

        // get the method used to obtain a PermissibleBase
        // this method will initialise a new PB instance if one doesn't yet exist
        Method getPermissibleBaseMethod = entityClass.getDeclaredMethod("getPermissibleBase");
        getPermissibleBaseMethod.setAccessible(true);

        // get the PermissibleBase instance
        PermissibleBase permBase = (PermissibleBase) getPermissibleBaseMethod.invoke(null);

        // get the perm field on CraftEntity
        Field permField = entityClass.getDeclaredField("perm");
        permField.setAccessible(true);

        // create a new instance which delegates to the previous PermissibleBase
        PermissibleBase newPermBase = new LegendPermissibleBase(player, groupPermissionManager, groupMemberManager);

        // inject the new instance
        permField.set(null, newPermBase);
    }
}