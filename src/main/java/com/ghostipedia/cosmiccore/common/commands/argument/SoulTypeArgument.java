package com.ghostipedia.cosmiccore.common.commands.argument;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SoulTypeArgument implements ArgumentType<SoulType> {

    private static final Collection<String> EXAMPLES = Arrays.stream(SoulType.values())
            .map(SoulType::getSerializedName)
            .collect(Collectors.toList());

    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
            (object) -> Component.translatable("argument.enum.invalid", object)
    );

    public static SoulTypeArgument soulType() {
        return new SoulTypeArgument();
    }

    public static SoulType get(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, SoulType.class);
    }

    @Override
    public SoulType parse(StringReader reader) throws CommandSyntaxException {
        String s = reader.readUnquotedString();
        SoulType soulType = SoulType.byName(s);
        if (soulType == null) {
            throw ERROR_INVALID_VALUE.create(s);
        }
        return soulType;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(EXAMPLES, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
