# TimeTrack

TimeTrack is an app that allows the user to log their hours on specific assignments for each course. It is designed to allow the
user to keep track of the amount of time spent on tasks and view the data in a practical way.

#Setup
In order to run TimeTrack on your Android Device or Emulator, download the master branch as a ZIP file. Unzip the file in a practical place.
Open Android Studios and select import project. Navigate to the unzipped file and select TimeTrack. The project will be imported. Navigate
to the run button and click run. Select your device of choice. The app will now run on your device.

#Use
####Classes
Begin by creating a `Class` with the name of your course. You select the class in order to begin adding tasks.


####Tasks
When creating a `Task`, Enter the name of the task and the predicted number of hours to complete the task. Upon selecting your created task,
you can begin logging hours using `START` and `STOP` buttons. You may also delete the task in order to clear all data related to the task. 
Once you are done logging data on your task, you may select `Finish` in order to archive the task. This keeps the data for that task, but 
removes it from the list of tasks.

#####Data
Once you have logged data and wish to view it, select `Display Data`. It will display data by class hours spent. You may select a slice of the
pie chart in order to display a message about the class. Selecting a class from the drop down and pressing the `View` button will bring you to
the data page for that specific class. It will display time spent on tasks for the selected class. Similarly, selecting a slice of the pie will
display additional data.

#####Clearing Data
There are two general ways of deleting time data. The first is to delete the task. This will clear all information about the task including time data.
The second way is to delete the class itself. This will clear all data related to the class, including all of its tasks. Once a task is finished
via the `Finish` button, the only way to delete the data is to delete the class itself.


