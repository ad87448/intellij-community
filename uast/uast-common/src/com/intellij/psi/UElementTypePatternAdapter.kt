// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.psi

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.RecursionManager
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.ElementPatternCondition
import com.intellij.patterns.InitialPatternCondition
import com.intellij.util.ProcessingContext
import org.jetbrains.uast.UElement

internal class UElementTypePatternAdapter(private val supportedUElementTypes: List<Class<out UElement>>) : ElementPattern<PsiElement> {
  override fun accepts(o: Any?): Boolean = accepts(o, null)

  override fun accepts(o: Any?, context: ProcessingContext?): Boolean {
    if (o !is PsiElement) return false
    if (context == null) {
      Logger.getInstance(UElementTypePatternAdapter::class.java).error("UElementTypePatternAdapter should not be called with null context")
      return false
    }

    return RecursionManager.doPreventingRecursion(this, false) {
      if (getOrCreateCachedElement(o, context, supportedUElementTypes) == null) return@doPreventingRecursion false
      context.put(REQUESTED_PSI_ELEMENT, o)
      true
    } ?: false
  }

  private val condition = ElementPatternCondition(object : InitialPatternCondition<PsiElement>(PsiElement::class.java) {
    override fun accepts(o: Any?, context: ProcessingContext?): Boolean = this@UElementTypePatternAdapter.accepts(o, context)
  })

  override fun getCondition(): ElementPatternCondition<PsiElement> = condition
}