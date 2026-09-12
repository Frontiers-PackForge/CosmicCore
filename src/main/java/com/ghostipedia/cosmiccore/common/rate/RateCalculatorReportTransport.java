package com.ghostipedia.cosmiccore.common.rate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class RateCalculatorReportTransport {

    static final int MAX_ENCODED_BYTES = 1024 * 1024 - 1024;
    static final int MAX_EXPANDED_BYTES = 16 * 1024 * 1024;
    private static final long MAX_NBT_HEAP = 128L * 1024 * 1024;

    public static final StreamCodec<FriendlyByteBuf, CompoundTag> CODEC = new StreamCodec<>() {

        @Override
        public CompoundTag decode(FriendlyByteBuf buffer) {
            byte[] compressed = buffer.readByteArray(MAX_ENCODED_BYTES);
            try (GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
                byte[] expanded = input.readNBytes(MAX_EXPANDED_BYTES + 1);
                if (expanded.length > MAX_EXPANDED_BYTES)
                    throw new DecoderException("Rate report exceeds expanded limit");
                try (DataInputStream data = new DataInputStream(new ByteArrayInputStream(expanded))) {
                    CompoundTag report = NbtIo.read(data, NbtAccounter.create(MAX_NBT_HEAP));
                    if (data.available() != 0) throw new DecoderException("Trailing rate report data");
                    return report;
                }
            } catch (IOException exception) {
                throw new DecoderException("Invalid rate report", exception);
            }
        }

        @Override
        public void encode(FriendlyByteBuf buffer, CompoundTag report) {
            byte[] encoded = report.sizeInBytes() <= MAX_NBT_HEAP ? compress(report) : null;
            if (encoded == null) encoded = compress(unavailable(report));
            if (encoded == null) throw new EncoderException("Rate report header exceeds transport limit");
            buffer.writeByteArray(encoded);
        }
    };

    private RateCalculatorReportTransport() {}

    private static byte[] compress(CompoundTag report) {
        ByteBuf expanded = Unpooled.buffer(4096, MAX_EXPANDED_BYTES);
        ByteBuf compressed = Unpooled.buffer(4096, MAX_ENCODED_BYTES);
        try {
            NbtIo.write(report, new ByteBufOutputStream(expanded));
            try (GZIPOutputStream output = new GZIPOutputStream(new ByteBufOutputStream(compressed))) {
                expanded.readBytes(output, expanded.readableBytes());
            }
            byte[] result = new byte[compressed.readableBytes()];
            compressed.readBytes(result);
            return result;
        } catch (IndexOutOfBoundsException exception) {
            return null;
        } catch (IOException exception) {
            throw new EncoderException("Cannot encode rate report", exception);
        } finally {
            expanded.release();
            compressed.release();
        }
    }

    static CompoundTag unavailable(CompoundTag report) {
        CompoundTag result = new CompoundTag();
        result.putString("dimension", report.getString("dimension"));
        for (String key : new String[] { "selected", "loaded", "unknown", "unloaded" })
            result.putInt(key, report.getInt(key));
        result.putLong("elapsedTicks", report.getLong("elapsedTicks"));
        result.put("machines", new ListTag());
        result.put("rows", new ListTag());
        result.putInt("machineCount", 0);
        result.putInt("rowCount", 0);
        result.putBoolean("partial", true);
        result.putBoolean("truncated", true);
        return result;
    }
}
