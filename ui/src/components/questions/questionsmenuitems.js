import React from 'react';
import { withRouter } from 'react-router-dom';
import { t } from 'ttag';

class QuestionsMenuItems extends React.Component {
    constructor(props) {
        super(props);
    }

    navigate(url) {
        this.props.history.push(url);
    }

    deletePost() {

    }

    render() {
        return (
            <ul>
                <li onClick={this.navigate.bind(this, `/create/question/${this.props.questionId}`)}>
                    <i className="fa fa-pencil" /> {t`Edit`}
                </li>
                <li onClick={this.deletePost.bind(this)}>
                    <i className="fa fa-trash" /> {t`Delete`}
                </li>
            </ul>
        );
    }
}

QuestionsMenuItems.defaultProps = {
    questionId: 0
};

export default withRouter(QuestionsMenuItems);
