import React from 'react'
import { t } from 'ttag';
import { withRouter } from 'react-router-dom';
import InputField from '../forms/inputfield';
import * as Question from '../../models/question';
import Loader from '../utils/loader';

const MAX_RESULTS = 5;

class HeaderSearch extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            searchStr: '',
            loadingSearch: false,
            searchResult: []
        };

        this.onClick = this.onClickCtx.bind(this);
    }

    componentDidMount() {
        window.addEventListener('click', this.onClick);
    }

    onClickCtx(e) {
        if (!this.node.contains(e.target)) {
            this.hideSearchResults();
        }
    }

    componentWillUnmount() {
        removeEventListener('click', this.onClick);
    }

    handleChange(node) {
        const value = node.target.value;
        const searchQuery = value.trim().toLowerCase();
        const oldSearchQuery = this.state.searchStr.trim().toLowerCase();

        this.setState({
            searchStr: value
        });

        if(oldSearchQuery === searchQuery) {
            return;
        }
        clearTimeout(this.searchTimer);

        if(searchQuery === '') {
            this.setState({
                loadingSearch: false,
                searchResult: [],
                autocompleteText: null
            });
            return;
        }

        this.setState({
            loadingSearch: true
        });

        this.searchTimer = setTimeout(() => {
            Question.searchQuestions(searchQuery, MAX_RESULTS).then((questions) => {
                this.setState({
                    searchResult: questions,
                    loadingSearch: false
                });
            });
        }, 700);
    }

    navigate(url) {
        this.setState({
            searchStr: '',
            searchResult: [],
        });
        this.props.history.push(url);
    }

    hideSearchResults() {
        this.setState({
            searchResult: []
        });
    }

    renderMoreButton() {
        if (this.state.searchResult.length === MAX_RESULTS) {
            return (
                <li className="more" onClick={this.navigate.bind(this, `/search/${this.state.searchStr}`)}>
                    {t`View more results`}
                </li>
            );
        }
    }

    renderDropDown() {
        if (this.state.loadingSearch) {
            return (
                <div className="field-auto-complete padded">
                    <Loader />
                </div>
            );
        }

        if (this.state.searchResult.length === 0) {
            return null;
        }

        return (
            <ul className="field-auto-complete">
                {this.state.searchResult.map(
                    (item, index) => {
                        return (
                            <li key={index} onClick={this.navigate.bind(this, `/question/${item.id}`)}>
                                {item.title}
                            </li>
                        );
                    })
                }
                {this.renderMoreButton()}
            </ul>
        );
    }

    render() {
        return (
            <div className="relative quick-search" ref={(node) => {this.node = node}}>
                <InputField
                    type="text"
                    icon="fa-search"
                    placeholder={t`Quicksearch`}
                    size="small"
                    value={this.state.searchStr}
                    onChange={this.handleChange.bind(this)}
                    className="flex-grow"
                />
                {this.renderDropDown()}
            </div>
        );
    }
}

export default withRouter(HeaderSearch);
