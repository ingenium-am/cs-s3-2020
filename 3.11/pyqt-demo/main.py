import sys
from PyQt5 import QtCore, QtWidgets


class MainWindow(QtWidgets.QWidget):

    def __init__(self, parent=None):
        super(MainWindow, self).__init__(parent)

        self.setWindowTitle("DEMO")

        self.printLayout = QtWidgets.QLabel("")
        self.printLayout.setAlignment(QtCore.Qt.AlignCenter)

        self.printBtn = QtWidgets.QPushButton("Greet")
        self.printBtn.clicked.connect(self.on_click)

        self.mainLayout = QtWidgets.QVBoxLayout()
        self.mainLayout.addWidget(self.printLayout)
        self.mainLayout.addWidget(self.printBtn, 0, QtCore.Qt.AlignCenter)

        self.setFixedSize(300, 150)
        self.setLayout(self.mainLayout)

    @QtCore.pyqtSlot()
    def on_click(self):
        self.printLayout.setText("Hello from PyQt!")


if __name__ == "__main__":
    app = QtWidgets.QApplication(["demo.app"])
    mainWindow = MainWindow()
    mainWindow.show()

    app.setActiveWindow(mainWindow)

    sys.exit(app.exec_())
