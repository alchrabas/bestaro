import React from 'react';
import { connect } from 'react-redux';
import WelcomePageContainer from './WelcomePageContainer';
import ReadMorePageContainer from './ReadMorePageContainer';
import PrivacyPolicyPageContainer from './PrivacyPolicyPageContainer';
import { BrowserRouter, Redirect, Route, Switch } from 'react-router-dom';
import NarrowMapPageContainer from './NarrowMapPageContainer';
import WideMapPageContainer from './WideMapPageContainer';
import { withRouter } from 'react-router';
import { compose } from 'redux';
import i18n from '../i18n';

const EnglishOrNot = withRouter(
    class extends React.Component {
        componentDidMount() {
            this.unlisten = this.props.history.listen((location, action) => {
                if (location.pathname.indexOf('/en') === 0) {
                    if (i18n.language === 'pl') {
                        i18n.changeLanguage('en');
                    }
                } else if (i18n.language === 'en') {
                    i18n.changeLanguage('pl');
                }
            });
        }

        componentWillUnmount() {
            this.unlisten();
        }

        render() {
            const { wide, match } = this.props;
            const matchUrl = match.url.endsWith('/') ? match.url.slice(0, -1) : match.url;
            return <Switch>
                <Route exact path={`${matchUrl}/`} component={WelcomePageContainer} />
                <Route path={`${matchUrl}/map`} component={wide ? WideMapPageContainer : NarrowMapPageContainer} />
                <Route path={`${matchUrl}/read-more`} component={ReadMorePageContainer} />
                <Route path={`${matchUrl}/privacy-policy`} component={PrivacyPolicyPageContainer} />
                <Redirect from={`${matchUrl}/list`} to="/map" />
            </Switch>;
        }
    });


const mapStateToProps = (state) => ({
    wide: state.responsive.isWide,
});

const EnglishOrNotContainer = compose(
    withRouter,
    connect(mapStateToProps),
)(EnglishOrNot);


const App = () => {
    return (
        <BrowserRouter>
            <Switch>
                <Route path="/en/" render={(props) => <EnglishOrNotContainer {...props} />} />
                <Route path="/pl/" render={(props) => <EnglishOrNotContainer {...props} />} />
                <Route path="/" render={(props) => <EnglishOrNotContainer {...props} />} />
            </Switch>
        </BrowserRouter>
    );
};


const AppContainer = App;

export default AppContainer;
