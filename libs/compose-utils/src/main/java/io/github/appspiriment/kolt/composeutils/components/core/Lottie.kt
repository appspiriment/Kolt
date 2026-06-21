//package io.github.appspiriment.kolt.composeutils.components.core
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import com.airbnb.lottie.compose.LottieAnimation
//import com.airbnb.lottie.compose.LottieCompositionSpec
//import com.airbnb.lottie.compose.LottieConstants
//import com.airbnb.lottie.compose.rememberLottieComposition
//import io.github.appspiriment.kolt.composeutils.R
//
//@Composable
//fun Lottie(rawResId: Int, modifier: Modifier = Modifier,  iterations : Int = LottieConstants.IterateForever){
//    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawResId))
//    LottieAnimation(composition,modifier, iterations = iterations)
//}
//
//
//@Preview
//@Composable
//fun PreviewLottie() {
//    Lottie(rawResId = R.raw.lottie_not_found)
//}