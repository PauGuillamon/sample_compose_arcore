# Sample Compose ARCore

This project contains an AR Android application that showcases how to combine Compose, OpenGL ES and ARCore.

The different samples build up from a basic OpenGL view with compose, to a full AR experience with ARCore and OpenGL ES.

This project was created for my "Building an AR app with Compose & OpenGL ES" talk for Droidcon Berlin 2024.

# Features

The main screen uses Compose's NavHost to navigate through the different samples.

The sample called `Sample5_ARCore` includes all the following features:
- Integrating a OpenGL view with Compose and passing the renderer through a ViewModel
- Avoiding recompositions of the OpenGL view when something else from the ViewModel has changes
- AR:
  - Creation and setup of the ARCore session including:
    - Denabling Depth API on supported evices
    - On demand enabling Electronic Image Stabilization (EIS).
    - Visualization of detected feature points
  - Performs hitTest on touch events and create new Anchors and nodes in the scene
  - Button to delete the last created object
  - Virtual objects occluded by real world objects, depth occlusion (only on Depth API supported devices).
  - Uses framebuffers to simplify the use of the depth map, especially useful when EIS is enabled.
