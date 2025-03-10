package com.project.cryptowallet.login.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthScreen(
    title: String,
    subtitle: String,
    primaryButtonText: String,
    secondaryText: String,
    onPrimaryButtonClick: (String, String) -> Unit,
    onSecondaryButtonClick: () -> Unit,
    authError: String?, // Error message from ViewModel
    resetError: () -> Unit // Function to clear the error
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val emailError = remember { mutableStateOf<String?>(null) }
    val passwordError = remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Show Toast if there's an error
    LaunchedEffect(authError) {
        authError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            resetError() // Clear the error after showing
        }
    }


    fun validateAndSubmit() {
        emailError.value = if (email.value.isBlank()) "Email cannot be empty" else null
        passwordError.value = if (password.value.isBlank()) "Password cannot be empty" else null

        if (emailError.value == null && passwordError.value == null) {
            onPrimaryButtonClick(email.value, password.value)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .alpha(0.7f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.background)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AuthHeader(title = title, subtitle = subtitle)

                Spacer(modifier = Modifier.height(16.dp))

                AuthFields(
                    email = email.value,
                    password = password.value,
                    emailError = emailError.value,
                    passwordError = passwordError.value,
                    onEmailChange = {
                        email.value = it
                        emailError.value = null
                    },
                    onPasswordChange = {
                        password.value = it
                        passwordError.value = null
                    },
                    onForgotPasswordClick = {}
                )

                AuthFooter(
                    primaryButtonText = primaryButtonText,
                    secondaryText = secondaryText,
                    onPrimaryButtonClick = { validateAndSubmit() },
                    onSecondaryButtonClick = onSecondaryButtonClick
                )
            }
        }
    }
}





@Composable
fun AuthHeader(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        Text(text = subtitle, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp, top = 8.dp))
    }
}

@Composable
fun AuthFooter(
    primaryButtonText: String,
    secondaryText: String,
    onPrimaryButtonClick: () -> Unit,
    onSecondaryButtonClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = onPrimaryButtonClick, modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp)) {
            Text(text = primaryButtonText, fontSize = 16.sp)
        }
        TextButton(onClick = onSecondaryButtonClick, contentPadding = PaddingValues(0.dp)) {
            Text(text = secondaryText)
        }
    }
}

@Composable
fun AuthFields(
    email: String,
    password: String,
    emailError: String?,
    passwordError: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Column {
        TextField(
            value = email,
            label = "Email",
            placeholder = "Enter your email address",
            onValueChange = onEmailChange,
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = emailError != null
        )
        emailError?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = password,
            label = "Password",
            placeholder = "Enter your password",
            onValueChange = onPasswordChange,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go
            ),
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Password")
            },
            isError = passwordError != null
        )
        passwordError?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }

        TextButton(onClick = onForgotPasswordClick, modifier = Modifier.align(Alignment.End)) {
            Text(text = "Forgot Password?")
        }
    }
}


@Composable
fun TextField(
    value: String,
    label: String,
    placeholder: String,
    fontSize: TextUnit = 14.sp,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, fontSize = fontSize) },
        placeholder = { Text(text = placeholder, fontSize = fontSize) },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(16.dp),
        isError = isError, // Highlight text field in red if error
        modifier = modifier
    )
}


@Preview
@Composable
private fun AuthScreenPreview() {
    val signup:String = "Sign Up"
    AuthScreen(
        title = "Welcome",
        subtitle = "Sign up to continue",
        primaryButtonText = "Sign Up",
        secondaryText = "Already have an account, click here",
        onPrimaryButtonClick = {signup, string -> },
        onSecondaryButtonClick = {},
        authError = "Error message",
        resetError = {}

    )

}
