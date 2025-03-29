package com.example.recipehub.fragments
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import com.example.recipehub.databinding.FragmentRegisterBinding
import androidx.navigation.fragment.findNavController
import com.example.recipehub.R
import com.example.recipehub.model.UserModel
import com.example.recipehub.model.User
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.material.BottomNavigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.findNavController
import androidx.core.net.toUri
import com.example.recipehub.utils.saveBitmapToFile
import com.example.recipehub.utils.uriToBitmap
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.recipehub.utils.SimulateLoading
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import com.example.recipehub.utils.setupUI
import com.example.recipehub.utils.getCorrectlyOrientedBitmap

class RegisterFragment: BaseFragment(){
    private var binding : FragmentRegisterBinding? = null
    private lateinit var avatarUri: String
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var onRegistrationSuccess:Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
        ) : View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding?.refLoginTextView?.setOnClickListener{
            findNavController().navigate(R.id.action_to_login)
        }
        avatarUri = blankAvatar()
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let { selectedUri ->
                    val rotatedBitmap = getCorrectlyOrientedBitmap(requireContext(), selectedUri)
                    rotatedBitmap?.let { correctedBitmap ->
                        val fileUri = saveBitmapToFile(correctedBitmap, requireContext())
                        fileUri?.let { uri ->
                            avatarUri = uri.toString()
                            binding?.avatarImageView?.setImageURI(uri)
                            binding?.plusSignIcon?.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }
        binding?.avatarImageView?.setOnClickListener{ val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent) }
        binding?.registerBtn?.setOnClickListener{ onRegister() }


        return binding?.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().setupUI(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    @OptIn(UnstableApi::class)
    private fun onRegister() {
        val username = binding?.usernameEditText?.text.toString()
        binding?.usernameEditText?.requestFocus()
        val email = binding?.emailEditText?.text.toString()
        val password = binding?.passwordEditText?.text.toString()

        if (username.isEmpty() || username.length < 4) {
            binding?.usernameEditText?.error = "Username must be at least 4 characters"
            binding?.usernameEditText?.requestFocus()
            return
        }
        if (email.isEmpty()) {
            binding?.emailEditText?.error = "Email is required"
            binding?.emailEditText?.requestFocus()
            return
        }
        val emailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
        if ((!email.contains("@") || !email.contains(".")) && !email.matches(emailPattern)) {
            binding?.emailEditText?.requestFocus()
            binding?.emailEditText?.error = "Valid email is required"
            return
        }
        if (password.isEmpty()) {
            binding?.passwordEditText?.error = "Password is required"
            binding?.passwordEditText?.requestFocus()
            return
        }
        if (password.length < 6 || !password.contains(Regex(".*[A-Z].*"))) {
            binding?.passwordEditText?.error =
                "Password must be at least 6 characters and contain at least one capital letter"
            binding?.passwordEditText?.requestFocus()
            return
        }
        val user = User(
            username = username,
            email = email,
            password = password,
            avatarUrl = avatarUri,
        )

        binding?.registerBtn?.isEnabled = false

        UserModel.shared.addUser(user, {
            Log.d("Register", "User added")
            bottomNavController?.hideBottomNav()
            binding?.composeView?.visibility = View.VISIBLE
            binding?.composeView?.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    var isLoading by remember { mutableStateOf(true) }
                    SimulateLoading(
                        onLoadingComplete = { isLoading = false }, 10
                    )
                    LaunchedEffect(isLoading) {
                        if (!isLoading) {
                            findNavController().navigate(R.id.action_to_login)
                            try {
                                bottomNavController?.updateBottomNavSelection(R.id.login)
                                bottomNavController?.showBottomNav()
                            } catch (e: Exception) {
                                Log.e("RegisterFragment", "Error updating bottom nav: ${e.message}")

                            }
                        }
                    }
                }
            }

        }, { error ->
            binding?.registerBtn?.isEnabled = true
            if (error.message?.contains("Username", true) == true) {
                binding?.usernameEditText?.error = "Username already exists"
                binding?.usernameEditText?.requestFocus()
            } else if (error.message?.contains("email", true) == true) {
                binding?.emailEditText?.error = "Email already exists"
                binding?.emailEditText?.requestFocus()
            } else {
                Log.e("Register", "Error adding user: $error")
            }
            binding?.composeView?.visibility = View.GONE
            bottomNavController?.showBottomNav()
        })
    }
    private fun blankAvatar(): String {
        val blankAvatar = "android.resource://com.example.recipehub/drawable/blank_avatar"
        val bitmap = uriToBitmap(blankAvatar.toUri(), requireContext())
        bitmap?.let {
            val fileUri = saveBitmapToFile(it, requireContext())
            fileUri?.let { uri ->
                return uri.toString()
            }
        }
        return ""
    }



}