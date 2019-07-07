import React from "react";
import {connect} from "react-redux";
import WelcomePageContainer from "./WelcomePageContainer";
import ReadMorePageContainer from "./ReadMorePageContainer";
import PrivacyPolicyPageContainer from "./PrivacyPolicyPageContainer";
import {BrowserRouter, Redirect, Route, Switch} from 'react-router-dom';
import NarrowMapPageContainer from "./NarrowMapPageContainer";
import WideMapPageContainer from "./WideMapPageContainer";

const EnglishOrNot = ({wide, match}) => {
    const matchUrl = match.url.endsWith("/") ? match.url.slice(0, -1) : match.url;
    return <Switch>
        <Route exact path={`${matchUrl}/`} component={WelcomePageContainer}/>
        <Route path={`${matchUrl}/map`} component={wide ? WideMapPageContainer : NarrowMapPageContainer}/>
        <Route path={`${matchUrl}/read-more`} component={ReadMorePageContainer} />
        <Route path={`${matchUrl}/privacy-policy`} component={PrivacyPolicyPageContainer}/>
        <Redirect from={`${matchUrl}/list`} to="/map"/>
    </Switch>;
};

const App = ({wide}) => {
    return (
        <BrowserRouter>
            <Switch>
                <Route path="/en/" render={(props) => <EnglishOrNot {...props} wide={wide}/>}/>
                <Route path="/pl/" render={(props) => <EnglishOrNot {...props} wide={wide}/>}/>
                <Route path="/" render={(props) => <EnglishOrNot {...props} wide={wide}/>}/>
            </Switch>
        </BrowserRouter>
    );
};


const mapStateToProps = (state) => ({
    wide: state.responsive.isWide,
});

const AppContainer = connect(mapStateToProps)(App);

export default AppContainer;
