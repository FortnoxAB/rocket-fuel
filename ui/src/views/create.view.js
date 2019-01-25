import React from 'react';
import InputField from '../components/inputfield';
import Button from '../components/button';
import Loader from '../components/utils/loader';

class CreateView extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			title: '',
			description: '',
			bounty: 0,
			postingThread: false
		};
	}

	handleChange(target) {
		const value = target.value;
		const name = target.name;
		this.setState({
			[name]: value
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

	render() {

		if(this.state.postingThread) {
			return <Loader />
		}
		
		return (
			<div className="content">
				<div className="flex-row">

					<div className="form">
						<InputField
							onChange={this.handleChange.bind(this)}
							name="title"
							type="text"
							value={this.state.title}
							label="Titel"
						/>
						<InputField
							onChange={this.handleChange.bind(this)}
							name="description"
							type="textarea"
							height="200"
							value={this.state.description}
							label="Beskrivning"
						/>
						<InputField
							onChange={this.handleChange.bind(this)}
							name="bounty"
							type="number"
							value={this.state.bounty}
							label="Värde"
						/>
						<Button onClick={ () => this.saveThread.bind(this)}>Skicka fråga</Button>
					</div>

					<div className="info-square">
						<h3>Headline</h3>
						<p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Aspernatur dolore harum ipsum natus provident repellendus? At eos et, explicabo facere facilis fugit laborum laudantium obcaecati officia quas suscipit velit, veritatis voluptas. Cupiditate dolor doloribus esse, excepturi mollitia nesciunt nihil quas.</p>
					</div>
				</div>
			</div>
		);
	}
}

export default CreateView;
