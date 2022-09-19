package com.simibubi.create.content.contraptions.components.structureMovement.gantry;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.CachedPartialBuffers;
import com.simibubi.create.foundation.render.FlwSuperByteBuffer;

import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.math.AngleHelper;
import net.createmod.ponder.utility.WorldTickHolder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.BlockState;

public class GantryCarriageRenderer extends KineticTileEntityRenderer {

	public GantryCarriageRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		if (Backend.canUseInstancing(te.getLevel())) return;

		BlockState state = te.getBlockState();
		Direction facing = state.getValue(GantryCarriageBlock.FACING);
		Boolean alongFirst = state.getValue(GantryCarriageBlock.AXIS_ALONG_FIRST_COORDINATE);
		Axis rotationAxis = getRotationAxisOf(te);
		BlockPos visualPos = facing.getAxisDirection() == AxisDirection.POSITIVE ? te.getBlockPos()
				: te.getBlockPos()
				.relative(facing.getOpposite());
		float angleForTe = getAngleForTe(te, visualPos, rotationAxis);

		Axis gantryAxis = Axis.X;
		for (Axis axis : Iterate.axes)
			if (axis != rotationAxis && axis != facing.getAxis())
				gantryAxis = axis;

		if (gantryAxis == Axis.X)
			if (facing == Direction.UP)
				angleForTe *= -1;
		if (gantryAxis == Axis.Y)
			if (facing == Direction.NORTH || facing == Direction.EAST)
				angleForTe *= -1;

		SuperByteBuffer cogs = CachedPartialBuffers.partial(AllBlockPartials.GANTRY_COGS, state);
		float finalAngleForTe = angleForTe;
		FlwSuperByteBuffer.cast(cogs).ifPresent(flwBuffer -> flwBuffer
				.centre()
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
				.rotateY(alongFirst ^ facing.getAxis() == Axis.X ? 0 : 90)
				.translate(0, -9 / 16f, 0)
				.rotateX(-finalAngleForTe)
				.translate(0, 9 / 16f, 0)
				.unCentre()
		);

		cogs.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));

	}

	public static float getAngleForTe(KineticTileEntity te, final BlockPos pos, Axis axis) {
		float time = WorldTickHolder.getRenderTime(te.getLevel());
		float offset = getRotationOffsetForPosition(te, pos, axis);
		return (time * te.getSpeed() * 3f / 20 + offset) % 360;
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return shaft(getRotationAxisOf(te));
	}

}
