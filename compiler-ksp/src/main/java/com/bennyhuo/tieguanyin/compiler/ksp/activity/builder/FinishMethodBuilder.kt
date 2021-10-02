package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY_COMPAT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.ksp.utils.isDefault
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toJavaTypeName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

class FinishMethodBuilder(private val activityClass: ActivityClass) {

    fun build(typeBuilder: TypeSpec.Builder) {
        val finishMethodBuilder = MethodSpec.methodBuilder("smartFinish")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(activityClass.typeElement.toJavaTypeName(), "activity")

        //handle result parameters.
        activityClass.resultParameters.also{
            if(it.isNotEmpty()){
                finishMethodBuilder.addStatement("\$T intent = new \$T()", INTENT.java, INTENT.java)
                        .addStatement("activity.setResult(1, intent)")
            }
        }.forEach {
            resultParameter ->
            finishMethodBuilder.addParameter(resultParameter.javaTypeName, resultParameter.name)
            finishMethodBuilder.addStatement("intent.putExtra(\$S, \$L)", resultParameter.name, resultParameter.name)
        }

        finishMethodBuilder.addStatement("\$T.finishAfterTransition(activity)", ACTIVITY_COMPAT.java)

        //handle pending transitions.
        val pendingTransitionOnFinish = activityClass.pendingTransitionOnFinish
        if (!pendingTransitionOnFinish.isDefault()) {
            finishMethodBuilder.addStatement("activity.overridePendingTransition(\$L, \$L)", pendingTransitionOnFinish.enterAnim, pendingTransitionOnFinish.exitAnim)
        }
        typeBuilder.addMethod(finishMethodBuilder.build())
    }
}