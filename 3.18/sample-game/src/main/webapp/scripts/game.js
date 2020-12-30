(function() {
    'use strict';

    // Some variables need to be set on the server side (e.g. boardSize, gameHex)
    // See game.jsp


    const gameCache = {
        boardState: []  // init as empty array to fill on play
    };


    // * WEBSOCKET CONFIG

    const location = window.location;
    const protocol = location.protocol === 'https:' ? 'wss://' : 'ws://';

    // Creating endpoint address (e.g. ws://0.0.0.0:8080/websocket/14h6de154f)
    const wsEndpoint = protocol + location.host + '/websocket/' + gameHex;
    const socket = new WebSocket(wsEndpoint);

    socket.binaryType = 'arraybuffer';

    // Invoke the function after connection is open
    socket.onopen = function (event) {
        // Print info in console (F12 in browser -> Console)
        console.log('WebSocket Connected!');
        console.log('Connection is ' + (event.isTrusted ? 'Trusted' : 'NOT Trusted') + ': ' + wsEndpoint);

        // Set info line tot its default
        resetInfoLine();
    };

    // Invoke the function after receiving a message
    socket.onmessage = function (event) {
        // Print received message in console (F12 in browser -> Console)
        console.log('Message received: ' + event.data);

        // Parse message as JSON object and pass to the handler function
        let messageIn = JSON.parse(event.data);
        handleMessages(messageIn);
    };

    // Other WebSocket methods may be set as well:
    // socket.onclose
    // socket.onerror


    // * CANVAS CONFIG

    let canvas;                                                     // game-board canvas
    let context;                                                    // canvas context

    const tileSize = 30;                                            // 30px
    const borderAdd = 10;                                           // additional 10px as border
    const boardColor = 'green';
    const linesColor = '#005500';
    const boardSizePx = boardSize * tileSize + 2 * borderAdd + 1;   // + 1 to compensate 0.5px alignment
    const gridStart = 0.5 + borderAdd + tileSize / 2;               // + 0.5 to align to the pixel grid
    const gridEnd = boardSizePx - gridStart;

    const stoneSize = 29;                                           // stone picture size in px

    const aperture = tileSize - tileSize / 4;                       // distance from crossing to snap

    // Initialization function (see end of the file - window.onload)
    function initCanvas() {

        canvas = document.getElementById('grid-canvas');
        context = canvas.getContext('2d');

        canvas.width = boardSizePx;
        canvas.height = boardSizePx;

        // Pick a color, define rectangle with picked color, fill defined shape
        context.fillStyle = boardColor;
        context.fillRect(0, 0, canvas.width, canvas.height);
        context.fill();

        // Define horizontal lines on the board
        for (let row = gridStart; row < canvas.height; row += tileSize) {
            context.moveTo(gridStart, row);
            context.lineTo(gridEnd, row);
        }

        // Define vertical lines on the board
        for (let col = gridStart; col < canvas.height; col += tileSize) {
            context.moveTo(col, gridStart);
            context.lineTo(col, gridEnd);
        }

        context.strokeStyle = linesColor;                           // pick a color
        context.stroke();                                           // stroke defined lines

        // For 19x19 board draw star-points (circles)
        if (boardSize === 19) {
            let starPoints = [3, 9, 15];                            // grid steps for star-points
            starPoints.forEach(col => {                             // loop for columns
                starPoints.forEach(row => {                         // loop for rows
                    context.fillStyle = linesColor;
                    context.beginPath();
                    context.arc(numToPx(col), numToPx(row), 3, 0, 2 * Math.PI);
                    context.fill();
                })
            })
        }

        // Add EVENT LISTENER (callback function) to canvas on mousedown (click) event
        canvas.addEventListener('mousedown', function (e) {

            // Get coordinates of mouse click
            let coords = getCursorPosition(canvas, e);

            // Convert to grid coordinates
            let col = pxToNum(coords.x);
            let row = pxToNum(coords.y);

            // If grid coordinates are valid
            if (isValid(col, row)) {

                // If click position is obvious - close to the crossing
                if (isInAperture(coords.x, coords.y, col, row)) {

                    // Create a key-value message object
                    let message = {
                        'type': 'MOVE',
                        'coords': [pxToNum(coords.x), pxToNum(coords.y)],
                        'fromId': localPlayerId,
                        'fromName': localPlayerName
                    }
                    // Send the message via websocket as a string
                    socket.send(JSON.stringify(message));
                }
                // If coordinates are NOT obvious
                else {
                    changeInfoLine('info', 'Point an unoccupied intersection')
                }
            }
        })

        // Try to draw board state (if websocket response is NOT received yet - state is empty)
        drawBoardState();
    }


    // May be called from 'socket.onmessage' or from 'initCanvas'
    function drawBoardState() {
        // If document is not loaded - will be skipped
        if (document.readyState === 'complete') {
            gameCache.boardState.forEach(stone => {
                let stoneImage = stone[2] === localPlayerId ? localPlayerStone : remotePlayerStone;
                putStone(context, stone[0], stone[1], stoneImage);
            });
        }
    }

    // Handle incoming message
    function handleMessages(message) {
        // To check all types see 'app.Events' on the server side
        // To check message content see 'server.websocket.WebsocketServer'

        switch (message['type']) {

            case 'INIT': {
                // Set boardState
                gameCache.boardState = message['board'];

                // Try to draw board state
                // (if page is NOT loaded yet - will be called also after load)
                drawBoardState();

                // If game is started - set opponent's online status
                if (message['started']) {
                    changeOnlineStatus(message['oppOnline'], null);
                }
                break;
            }

            case 'JOIN': {
                let remotePlayer = message['fromName'];
                changeOnlineStatus(true, remotePlayer)
                changeInfoLine('accent', remotePlayer + ' joined the game!');
                break;
            }

            case 'QUIT': {
                let remotePlayer = message['fromName'];
                changeOnlineStatus(false, remotePlayer)
                changeInfoLine('alert', remotePlayer + ' left the game!');
                break;
            }

            case 'MOVE': {
                moveHandler(message);
                break;
            }

            default: {  // ERROR type is included
                let text = message['text'];
                if (!text) {
                    text = 'Unknown error on the server side';
                }
                changeInfoLine('alert', text);
            }
        }
    }

    // Handle MOVE type message
    function moveHandler(message) {
        // To check all move statuses see 'app.game.MoveStatus' on the server side

        switch (message['status']) {

            // Multiple cases stack
            case 'SUCCESS':
            case 'DRAW':
            case 'WIN': {
                // Handle successful moves
                acceptedMoveHandler(message);
                break;
            }

            case 'FINISHED': {
                changeInfoLine('accent', 'The game is FINISHED!');
                break;
            }

            case 'NOT_STARTED': {
                changeInfoLine('alert', 'NOT started, waiting for player!');
                break;
            }

            case 'NOT_YOUR_TURN': {
                changeInfoLine('alert', 'NOT your turn!');
                break;
            }

            default: {  // NOT_AUTHORIZED and INVALID_MOVE statuses included
                changeInfoLine('alert', 'Invalid move!');
            }
        }
    }

    // Handle successful moves
    function acceptedMoveHandler(message) {

        // Get data from the message
        let fromId = message['fromId'];
        let col = message['coords'][0];
        let row = message['coords'][1];

        // Check stone's owner
        let stone = fromId === localPlayerId ? localPlayerStone : remotePlayerStone;

        // Put stone on the board and update the cache
        putStone(context, col, row, stone);
        gameCache.boardState.push({ col, row, fromId })

        // If game is going on
        if (message['status'] === 'SUCCESS') {
            switchTurnIndicator();
            if (fromId !== localPlayerId) {
                changeInfoLine('accent', 'Your turn!');
            }
        }

        // If game is finished - DRAW or WIN
        else {
            finishingHandler(message);
        }
    }

    // Handle game finishing - the move is the last
    function finishingHandler(message) {

        // If there is a winner
        if (message['status'] === 'WIN') {

            // Get data from message
            let winnerId = message['fromId'];
            // Get id prefix of html tag
            let winnerPrefix = winnerId === localPlayerId ? 'lp' : 'rp';

            // Get indicator element - indicator img element is not hidden (showing last turn)
            let winnerIndicatorElem = document.getElementById(winnerPrefix + '-turn-img');
            // Change image of indicator
            winnerIndicatorElem.src = '/img/star.png';

            // Show info depending on client-side
            if (winnerId === localPlayerId) {
                changeInfoLine('accent', 'You WIN!');
            } else {
                changeInfoLine('alert', 'You LOSE!');
            }
        }

        // DRAW case
        else {
            // Make both indicator elements hidden and show info
            document.getElementById('lp-turn-img').style.display = 'none';
            document.getElementById('rp-turn-img').style.display = 'none';
            changeInfoLine('alert', 'DRAW!');
        }
    }

    // Put stone on canvas context
    function putStone(context, col, row, stone) {
        let placementCoords = numsToPlacementCoords(col, row);
        context.drawImage(stone, placementCoords.x, placementCoords.y, stoneSize, stoneSize);
    }

    // Get canvas-related pixel coordinates of event
    function getCursorPosition(canvas, event) {
        let rect = canvas.getBoundingClientRect()
        return { x: event.clientX - rect.left, y: event.clientY - rect.top };
    }

    // Change remote player name color depending on online status
    function changeOnlineStatus(status, playerName) {
        // Get remote player name element
        let remotePNameElem = document.getElementById('rp-name');

        // If remote player is unknown - remove class (set on the server side in jsp)
        if (remotePNameElem.classList.contains('unknown') && playerName != null) {
            remotePNameElem.classList.remove('unknown');
            remotePNameElem.innerText = playerName;
        }
        // Set class by passed argument (see styles.css)
        remotePNameElem.className = status ? 'online' : 'offline';
    }

    // Change info line text and color
    function changeInfoLine(type, text) {
        let infoElem = document.getElementById('info');

        infoElem.className = type;                  // set class to color by CSS
        infoElem.innerText = text;                  // set text content

        // If type is not ERROR
        if (type !== 'ERROR') {
            // Set timeout to reset info line after 2.5s
            setTimeout(function () {
                resetInfoLine(infoElem);
            }, 2500);
         }
    }

    // Reset info line to its default
    function resetInfoLine() {
        let infoElem = document.getElementById('info');

        infoElem.className = 'info';                // Set default class
        infoElem.innerText = 'CONNECTED';           // Set default info
    }

    // Switch turn indicator after successful move
    function switchTurnIndicator() {
        let lpTurnElem = document.getElementById('lp-turn-img');
        let rpTurnElem = document.getElementById('rp-turn-img');

        if (lpTurnElem.style.display === 'none') {
            lpTurnElem.style.display = 'block';
            rpTurnElem.style.display = 'none';
        } else {
            lpTurnElem.style.display = 'none';
            rpTurnElem.style.display = 'block';
        }
    }

    // Convert canvas grid coordinate to canvas pixel coordinate
    function numToPx(numOfTile) {
        return gridStart + numOfTile * tileSize;
    }

    // Convert canvas pixel coordinate to canvas grid coordinate
    function pxToNum(px) {
        return Math.round((px - gridStart) / tileSize);
    }

    /* Check if a pixel (x, y) is enough close (<= aperture / 2) to the crossing (col, row),
       to avoid non-obvious pointing (middle of crossings) */
    function isInAperture(x, y, col, row) {
        return Math.hypot(x - numToPx(col), y - numToPx(row)) <= aperture / 2;
    }

    // Convert grid coordinates to pixel coordinates of stone image placing
    function numsToPlacementCoords(col, row) {
        return {
            x: numToPx(col) - stoneSize / 2,
            y: numToPx(row) - stoneSize / 2
        };
    }

    // Check if grid coordinates are valid
    function isValid(col, row) {

        // Check if coors are out of border
        if (col < 0 || row < 0 || col >= boardSize || row >= boardSize) {
            return false;
        }

        // Check if tile (crossing) is free
        let valid = true;
        for (let i = 0; i < gameCache.boardState.length; i++) {
            let pair = gameCache.boardState[i];
            if (pair.x === col && pair.y === row) {
                valid = false;
                break;
            }
        }
        return valid;
    }


    // ENTRY POINT after DOM (HTML) is loaded
    window.onload = function() {
        initCanvas();
    };

})();
// Encapsulate variables inside a function scope (not available globally),
// and use IIFE (Immediately-invoked Function Expression)
// (function() { ... })();