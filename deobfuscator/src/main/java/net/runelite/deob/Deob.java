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
package net.runelite.deob;

import com.google.common.base.Stopwatch;
import java.io.File;
import java.io.IOException;

import net.runelite.asm.ClassFile;
import net.runelite.asm.ClassGroup;
import net.runelite.asm.execution.Execution;
import net.runelite.deob.deobfuscators.FieldInliner;
import net.runelite.deob.deobfuscators.IllegalStateExceptions;
import net.runelite.deob.deobfuscators.Order;
import net.runelite.deob.deobfuscators.RenameUnique;
import net.runelite.deob.deobfuscators.RuntimeExceptions;
import net.runelite.deob.deobfuscators.UnreachedCode;
import net.runelite.deob.deobfuscators.UnusedClass;
import net.runelite.deob.deobfuscators.UnusedMethods;
import net.runelite.deob.deobfuscators.arithmetic.ModArith;
import net.runelite.deob.deobfuscators.arithmetic.MultiplicationDeobfuscator;
import net.runelite.deob.deobfuscators.arithmetic.MultiplyOneDeobfuscator;
import net.runelite.deob.deobfuscators.arithmetic.MultiplyZeroDeobfuscator;
import net.runelite.deob.deobfuscators.cfg.ControlFlowDeobfuscator;
import net.runelite.deob.util.JarUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Deob
{
	private static final Logger logger = LoggerFactory.getLogger(Deob.class);

	public static final int OBFUSCATED_NAME_MAX_LEN = 3;
	private static final boolean CHECK_EXEC = false;

	public static void main(String[] args) throws IOException
	{
		if (args == null || args.length < 2)
		{
			System.err.println("Syntax: input_jar output_jar");
			System.exit(-1);
		}

		logger.info("Deobfuscator revision {}", 194);

		Stopwatch stopwatch = Stopwatch.createStarted();

		ClassGroup group = JarUtil.load(new File(args[0]));

		ClassGroup filtered = new ClassGroup();
		ClassGroup removed = new ClassGroup();

		for (ClassFile cf: group)
		{
			if (cf.getName().contains("/"))
			{
				removed.addClass(cf);
			}
			else
				filtered.addClass(cf);

		}
		// remove except RuntimeException
		run(filtered, new RuntimeExceptions());

		run(filtered, new ControlFlowDeobfuscator());

		//run(group, new RenameUnique());

		// remove unused methods - this leaves Code with no instructions,
		// which is not valid, so unused methods is run after
		//run(group, new UnreachedCode());
		//run(group, new UnusedMethods());

		// remove illegal state exceptions, frees up some parameters
		//run(group, new IllegalStateExceptions());

		// remove constant logically dead parameters
		//run(group, new ConstantParameter());
		//TODO
		// remove unhit blocks
		//run(group, new UnreachedCode());
		//run(group, new UnusedMethods());

		// remove unused parameters
		//run(group, new UnusedParameters());

		// remove unused fields
		//run(group, new UnusedFields());

		run(filtered, new FieldInliner());

		// order uses class name order for sorting fields/methods,
		// so run it before removing classes below
		run(filtered, new Order());

		run(filtered, new UnusedClass());

		runMath(filtered);

		//run(group, new ExprArgOrder());

		//run(group, new Lvt());

		//run(group, new CastNull());

		//run(group, new EnumDeobfuscator());

		//new OpcodesTransformer().transform(group);

		//run(group, new MenuActionDeobfuscator());

		//new GetPathTransformer().transform(group);
		//new ClientErrorTransformer().transform(group);
		//new ReflectionTransformer().transform(group);

		for (ClassFile cf: removed)
			filtered.addClass(cf);
		JarUtil.save(group, new File(args[1]));

		stopwatch.stop();
		logger.info("Done in {}", stopwatch);
	}

	public static boolean isObfuscated(String name)
	{
		if (name.length() <= OBFUSCATED_NAME_MAX_LEN)
		{
			return !name.equals("run") && !name.equals("add");
		}
		return name.startsWith("method")
				|| name.startsWith("vmethod")
				|| name.startsWith("field")
				|| name.startsWith("class")
				|| name.startsWith("__");
	}

	private static void runMath(ClassGroup group)
	{
		ModArith mod = new ModArith();
		mod.run(group);

		int last = -1, cur;
		while ((cur = mod.runOnce()) > 0)
		{
			new MultiplicationDeobfuscator().run(group);

			// do not remove 1 * field so that ModArith can detect
			// the change in guessDecreasesConstants()
			new MultiplyOneDeobfuscator(true).run(group);

			new MultiplyZeroDeobfuscator().run(group);

			if (last == cur)
			{
				break;
			}

			last = cur;
		}

		// now that modarith is done, remove field * 1
		new MultiplyOneDeobfuscator(false).run(group);

		mod.annotateEncryption();
	}

	private static void run(ClassGroup group, Deobfuscator deob)
	{
		Stopwatch stopwatch = Stopwatch.createStarted();
		deob.run(group);
		stopwatch.stop();

		logger.info("{} took {}", deob.getClass().getSimpleName(), stopwatch);

		// check code is still correct
		if (CHECK_EXEC)
		{
			Execution execution = new Execution(group);
			execution.populateInitialMethods();
			execution.run();
		}
	}
}
