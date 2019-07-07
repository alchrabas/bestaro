import React from 'react';
import './App.css';

import { store } from './store';
import { Provider } from 'react-redux';
import 'purecss/build/pure-min.css';
import 'purecss/build/grids-responsive-min.css';
import 'react-virtualized/styles.css';
import AppContainer from './components/AppContainer';
import { dateToString } from './utils';

import { EVENT_ANY } from './constants';
import { mediaQueryTracker } from 'redux-mediaquery';
import { changeDateFilter } from './ducks/filter';


const yearAgoDate = new Date();
yearAgoDate.setDate(yearAgoDate.getDate() - 365);

store.dispatch(changeDateFilter(
    dateToString(yearAgoDate),
    dateToString(new Date()),
    EVENT_ANY
));

mediaQueryTracker({
    isWide: 'screen and (min-width: 1025px)',
    innerWidth: true,
    innerHeight: true,
}, store.dispatch);


function App() {
    return (
        <Provider store={store}>
            <AppContainer />
        </Provider>
    );
}

export default App;
