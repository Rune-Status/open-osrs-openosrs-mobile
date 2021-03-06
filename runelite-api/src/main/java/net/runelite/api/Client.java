/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.api;

import org.slf4j.Logger;

/**
 * Represents the RuneScape client.
 */
public interface Client
{
	/**
	 * Retrieve a global logger for the client.
	 * This is most useful for mixins which can't have their own.
	 */
	Logger getLogger();

	/**
	 * Gets the current game state as an int
	 *
	 * @return the game state
	 */
	GameState getGameState();

	Player getLocalPlayer();

    int getBaseX();

	int getBaseY();

	int getRSPlane();

	int getPlane();

	boolean isInInstancedRegion();

	int[][][] getInstanceTemplateChunks();

	CollisionData[] getCollisionMaps();

	Scene getScene();

	ObjectComposition getObjectComposition(int objectId);

	boolean isClientThread();

	ItemComposition getItemComposition(int itemId);

	int[][][] getTileHeights();

	byte[][][] getTileSettings();

	int getOverlayWidth();
	int getOverlayHeight();

	void setOverlayWidth(int width);
	void setOverlayHeight(int height);

	int getCameraZ();

	int getCameraPitch();

	int getCameraYaw();

	int getCameraY();

	int getCameraX();

	int getCameraZoom();

	int getViewportWidth();

	int getViewportHeight();

	String[] getDebugLines();

	void setDebugLines(String[] debugLines);
}
