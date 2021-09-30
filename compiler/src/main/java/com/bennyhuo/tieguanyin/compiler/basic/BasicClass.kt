package com.bennyhuo.tieguanyin.compiler.basic

import com.bennyhuo.aptutils.types.packageName
import com.bennyhuo.aptutils.types.simpleName
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.GenerateMode
import com.bennyhuo.tieguanyin.compiler.basic.entity.Field
import com.bennyhuo.tieguanyin.compiler.basic.entity.SharedElementEntity
import com.sun.tools.javac.code.Type
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import kotlin.collections.ArrayList

abstract class BasicClass(val typeElement: TypeElement) {

    val simpleName = typeElement.simpleName()

    val builderClassName: String by lazy {
        val list = ArrayList<String>()
        list += typeElement.simpleName()
        var element = typeElement.enclosingElement
        while (element != null && element.kind != ElementKind.PACKAGE){
            list += element.simpleName()
            element = element.enclosingElement
        }
        list.reversed().joinToString("_") + POSIX
    }
    val packageName: String = typeElement.packageName()

    private val declaredFields = TreeSet<Field>()
    private val declaredSharedElements = ArrayList<SharedElementEntity>()

    val generateMode: GenerateMode

    private var superClass: BasicClass? = null

    val isAbstract = typeElement.modifiers.contains(Modifier.ABSTRACT)

    init {
        val generateBuilder = typeElement.getAnnotation(Builder::class.java)
        generateMode = if (generateBuilder.mode == GenerateMode.Auto) {
            //如果有这个注解，说明就是 Kotlin 类
            val isKotlin = typeElement.getAnnotation(META_DATA) != null
            if (isKotlin) GenerateMode.Both else GenerateMode.JavaOnly
        } else generateBuilder.mode

        generateBuilder.sharedElements.mapTo(declaredSharedElements) { SharedElementEntity(it) }
        generateBuilder.sharedElementsByNames.mapTo(declaredSharedElements) { SharedElementEntity(it) }
        generateBuilder.sharedElementsWithName.mapTo(declaredSharedElements) { SharedElementEntity(it) }
    }

    val fields: TreeSet<Field> = TreeSet()

    val sharedElements = ArrayList(declaredSharedElements)

    fun <T: BasicClass> setUpSuperClass(classes: Map<Element, T>): T?{
        val typeMirror = typeElement.superclass?: return null
        if (typeMirror == Type.noType) return null
        val superClassElement = (typeMirror as DeclaredType).asElement() as TypeElement
        return classes[superClassElement].also { superClass ->
            superClass?.also {
                fields.addAll(it.fields)
                sharedElements += it.sharedElements
            }
            this.superClass = superClass
        }
    }

    fun addSymbol(field: Field) {
        declaredFields.add(field)
        fields.add(field)
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        val META_DATA = Class.forName("kotlin.Metadata") as Class<Annotation>
        const val POSIX = "Builder"
    }
}