// Enable Load or Join buttons on selector change (see lobby.jsp)
function buttonEnabler(id) {

    // Get selector and corresponding button elements by passed argument
    let selectElem = document.getElementById(id);
    let button = document.getElementById(id.split('-')[0] + '-button');

    // Set button disabled true if selection is the first (placeholder) element - false otherwise
    button.disabled = selectElem.selectedIndex === 0;
}