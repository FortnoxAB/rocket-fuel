import React from 'react';
import { t } from 'ttag';
import InputField from '../../components/inputfield';
import Button from '../../components/button';
import Markdown from '../../components/markdown';
import Loader from '../../components/utils/loader';

class CreatequestionView extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			title: '',
			description: '',
			bounty: 0,
			postingThread: false
		};
	}

	handleChange(node) {
		const target = node.target;
		const value = target.value;
		const name = target.name;
		this.setState({
			[name]: value
		});
	}

	handleChangeBounty(target) {
		let bountyValue = target.value;
		if (bountyValue < 0) {
			bountyValue = 0;
		}
		this.setState({
			bounty: bountyValue
		});
	}

	saveThread() {
		this.setState({
			postingThread:true
		});
		/*
		this.context.threads().addThread({
			title:this.state.title,
			description: this.state.description,
			bounty:this.state.bounty,
			created: Date.now()
		}).then((threadId)=> {
			window.location.replace(window.location.origin + "/thread/" + threadId);
		});
		*/
	}

	renderPreview() {
		if (!this.state.description && !this.state.title) {
			return null;
		}
		return (
			<div className="padded-vertical">
				<div className="underlined">{t`Preview`}</div>
				<h1>{this.state.title}</h1>
				<Markdown text={this.state.description} />
			</div>
		);
	}

	render() {
		if(this.state.postingThread) {
			return <Loader fillPage />
		}
		
		return (
			<div>
				<h1>{t`New post`}</h1>
				<div className="form">
					<InputField
						className="padded-bottom"
						placeholder={t`Title`}
						onChange={this.handleChange.bind(this)}
						name="title"
						type="text"
						value={this.state.title}
					/>
					<div className="padded-bottom">
						{t`Use Markdown in question field.`} <a href="https://guides.github.com/features/mastering-markdown/" target="_blank">{t`Markdown-syntax`}</a>
					</div>
					<InputField
						className="padded-bottom"
						placeholder={t`Question`}
						onChange={this.handleChange.bind(this)}
						name="description"
						type="textarea"
						height="200"
						value={this.state.description}
					/>
					<InputField
						className="padded-bottom"
						placeholder={t`Bounty`}
						onChange={this.handleChangeBounty.bind(this)}
						name="bounty"
						type="number"
						value={this.state.bounty}
					/>
					<Button color="secondary" onClick={this.saveThread.bind(this)}>{t`Post question`}</Button>
				</div>
				{this.renderPreview()}
			</div>
		);
	}
}

export default CreatequestionView;
