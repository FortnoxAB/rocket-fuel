import React from 'react';
import { withRouter } from 'react-router-dom';
import moment from 'moment';

import Certificate from '../utils/certificate';
import Coins from '../utils/coins';
import Trophy from '../utils/trophy';
import { UserContext } from '../../usercontext';
import Dropdown from '../utils/dropdown';
import { t } from 'ttag';
import Dialog from '../utils/dialog';
import Button from '../forms/button';
import * as User from '../../models/user';

class QuestionRow extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            user: null,
            isDeleteQuestionDialogOpen: false,
            isDeletingQuestion: false
        };
    }

    componentDidMount() {
        this.setState({
            user: this.context.state.user,
            isDropdownOpen: false
        });
    }

    getTime() {
        return moment(this.props.question.createdAt).fromNow();
    }

    getState() {
        if (this.props.question.answerAccepted) {
            return 'answered';
        }

        return 'unanswered';
    }

    printCertificate() {
        const hasAccepted = this.props.question.answerAccepted;
        return <Certificate active={hasAccepted} />;
    }

    printTrophy() {
        const hasEnoughVotes = this.props.question.votes > 20;
        return <Trophy active={hasEnoughVotes} />;
    }

    getClasses() {
        let classes = 'question-row flex';
        if (this.props.small) {
            classes = `${classes} small`;
        }
        return classes;
    }

    renderTags() {
        if (this.props.hideTags) {
            return null;
        }

        return (
            <div className="tags">
                <div className="tag">JavaScript</div>
                <div className="tag">ReactJs</div>
                <div className="tag">Frontend</div>
            </div>
        );
    }

    navigateToQuestion(questionId) {
        this.props.history.push(`/question/${questionId}`);
    }

    toggleDropdown() {
        this.setState({
            isDropdownOpen: !this.state.isDropdownOpen
        });
    }

    closeDropdown() {
        if (!this.state.isDropdownOpen) {
            return;
        }
        this.setState({
            isDropdownOpen: false
        });
    }

    editQuestion() {
        this.props.history.push(`/create/thread/${this.props.question.id}`);
    }

    deleteQuestion() {
        this.setState({
            isDeleteQuestionDialogOpen: true
        });
    }

    closeDialog() {
        this.setState({
            isDeleteQuestionDialogOpen: false
        });
    }

    delete() {
        this.setState({
            isDeletingQuestion: true
        });
        User.deleteQuestion(this.props.question.id).then(() => {
            this.setState({
                isDeletingQuestion: false,
                isDeleteQuestionDialogOpen: false
            });
            this.props.onDeleteQuestion();
        }).catch(() => {
            this.setState({
                isDeletingQuestion: false
            });
        });
    }

    renderRowMenu() {
        if (this.props.question.userId !== this.context.state.user.id) {
            return null;
        }
        return(
            <div className="flex question-menu">
                <div className="menu-button" onClick={this.toggleDropdown.bind(this)}>
                    <i className="fa fa-ellipsis-v" />
                </div>
                <Dropdown
                    isOpen={this.state.isDropdownOpen}
                    close={this.closeDropdown.bind(this)}
                >
                    <ul>
                        <li onClick={this.editQuestion.bind(this)}>
                            <i className="fa fa-pencil" /> {t`Edit`}
                        </li>
                        <li onClick={this.deleteQuestion.bind(this)}>
                            <i className="fa fa-trash" /> {t`Delete`}
                        </li>
                    </ul>
                </Dropdown>
            </div>
        );
    }

    render() {
        return (
            <div className={`${this.getClasses()} ${this.getState()}`}>
                <Dialog isOpen={this.state.isDeleteQuestionDialogOpen} title={t`Delete question`}>
                    <div className="padded-bottom-large">
                        <b>{this.props.question.title}</b> {t`will be deleted from Rocket fuel.`}
                    </div>
                    <div className="flex flex-end">
                        <Button onClick={this.closeDialog.bind(this)} text>{t`Cancel`}</Button>
                        <Button onClick={this.delete.bind(this)} loading={this.state.isDeletingQuestion} text color="primary">{t`Delete`}</Button>
                    </div>
                </Dialog>
                <div className="content" onClick={this.navigateToQuestion.bind(this, this.props.question.id)}>
                    {/*<Coins amount={this.props.question.bounty} />*/}
                    <div className="marks">
                        {this.printCertificate()}
                        {this.printTrophy()}
                    </div>
                    <div className="body">
                        <div className="title">
                            {this.props.question.title}
                        </div>
                        <div className="user">
                            {this.props.question.createdBy}, {this.getTime()}
                        </div>
                    </div>
                </div>
                {this.renderRowMenu()}
            </div>
        );
    }
}

QuestionRow.defaultProps = {
    user: {},
    question: {},
    small: false,
    hideTags: false,
    onDeleteQuestion: () => {}
};

export default withRouter(QuestionRow);

QuestionRow.contextType = UserContext;
