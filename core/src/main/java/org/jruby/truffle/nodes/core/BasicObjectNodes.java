/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import java.math.*;
import java.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.FrameInstance;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.control.RaiseException;
import org.jruby.truffle.runtime.core.*;

@CoreClass(name = "BasicObject")
public abstract class BasicObjectNodes {

    @CoreMethod(names = "!", needsSelf = false, maxArgs = 0)
    public abstract static class NotNode extends CoreMethodNode {

        public NotNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public NotNode(NotNode prev) {
            super(prev);
        }

        @Specialization
        public boolean not() {
            return false;
        }

    }

    @CoreMethod(names = "==", minArgs = 1, maxArgs = 1)
    public abstract static class EqualNode extends CoreMethodNode {

        public EqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public EqualNode(EqualNode prev) {
            super(prev);
        }

        @Specialization
        public boolean equal(Object a, Object b) {
            notDesignedForCompilation();

            // TODO(CS) ideally all classes would do this in their own nodes
            return a.equals(b);
        }

    }

    @CoreMethod(names = "!=", minArgs = 1, maxArgs = 1)
    public abstract static class NotEqualNode extends CoreMethodNode {

        public NotEqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public NotEqualNode(NotEqualNode prev) {
            super(prev);
        }

        @Specialization
        public boolean notEqual(Object a, Object b) {
            notDesignedForCompilation();

            // TODO(CS) ideally all classes would do this in their own nodes
            return !a.equals(b);
        }

    }

    @CoreMethod(names = "equal?", minArgs = 1, maxArgs = 1)
    public abstract static class ReferenceEqualNode extends CoreMethodNode {

        public ReferenceEqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ReferenceEqualNode(ReferenceEqualNode prev) {
            super(prev);
        }

        @Specialization(order = 1)
        public boolean equal(@SuppressWarnings("unused") NilPlaceholder a, @SuppressWarnings("unused") NilPlaceholder b) {
            return true;
        }

        @Specialization(order = 2)
        public boolean equal(boolean a, boolean b) {
            return a == b;
        }

        @Specialization(order = 3)
        public boolean equal(int a, int b) {
            return a == b;
        }

        @Specialization(order = 4)
        public boolean equal(double a, double b) {
            return a == b;
        }

        @Specialization(order = 5)
        public boolean equal(BigInteger a, BigInteger b) {
            return a.compareTo(b) == 0;
        }

        @Specialization(order = 6)
        public boolean equal(RubyBasicObject a, RubyBasicObject b) {
            return a == b;
        }
    }

    @CoreMethod(names = "initialize", needsSelf = false, maxArgs = 0)
    public abstract static class InitializeNode extends CoreMethodNode {

        public InitializeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public InitializeNode(InitializeNode prev) {
            super(prev);
        }

        @Specialization
        public NilPlaceholder initiailze() {
            return NilPlaceholder.INSTANCE;
        }

    }

    @CoreMethod(names = "method_missing", needsBlock = true, isSplatted = true)
    public abstract static class MethodMissingNode extends CoreMethodNode {

        public MethodMissingNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public MethodMissingNode(MethodMissingNode prev) {
            super(prev);
        }

        @Specialization
        public Object methodMissing(RubyBasicObject self, Object[] args, @SuppressWarnings("unused") UndefinedPlaceholder block) {
            notDesignedForCompilation();

            CompilerDirectives.transferToInterpreter();

            final RubySymbol name = (RubySymbol) args[0];
            final Object[] sentArgs = Arrays.copyOfRange(args, 1, args.length);
            return methodMissing(self, name, sentArgs, null);
        }

        @Specialization
        public Object methodMissing(RubyBasicObject self, Object[] args, RubyProc block) {
            notDesignedForCompilation();

            CompilerDirectives.transferToInterpreter();

            final RubySymbol name = (RubySymbol) args[0];
            final Object[] sentArgs = Arrays.copyOfRange(args, 1, args.length);
            return methodMissing(self, name, sentArgs, block);
        }

        private Object methodMissing(RubyBasicObject self, RubySymbol name, Object[] args, RubyProc block) {
            throw new RaiseException(getContext().getCoreLibrary().nameErrorNoMethod(name.toString(), self.toString()));
        }


    }

    @CoreMethod(names = {"send", "__send__"}, needsBlock = true, minArgs = 1, isSplatted = true)
    public abstract static class SendNode extends CoreMethodNode {

        public SendNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public SendNode(SendNode prev) {
            super(prev);
        }

        @Specialization
        public Object send(RubyBasicObject self, Object[] args, @SuppressWarnings("unused") UndefinedPlaceholder block) {
            notDesignedForCompilation();

            final String name = args[0].toString();
            final Object[] sendArgs = Arrays.copyOfRange(args, 1, args.length);
            return self.send(name, null, sendArgs);
        }

        @Specialization
        public Object send(RubyBasicObject self, Object[] args, RubyProc block) {
            notDesignedForCompilation();

            final String name = args[0].toString();
            final Object[] sendArgs = Arrays.copyOfRange(args, 1, args.length);
            return self.send(name, block, sendArgs);
        }

    }

}
