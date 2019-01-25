import React from 'react';

class InputField extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			active: false
		};
	}

	componentDidMount() {
		this.checkActive();
		window.addEventListener('mousedown', this.click.bind(this), false);
	}

	componentWillUnmount() {
		window.removeEventListener('mousedown', this.click.bind(this), false);
	}

	click(e) {
		if (!this.node) {
			return;
		}
		const isClickInsideNode = this.node.contains(e.target);
		if (!isClickInsideNode) {
			this.setState({ active: false });
		}
	}

	componentDidUpdate(prevProps) {
		if (this.props.value !== prevProps.value) {
			this.checkActive();
		}
	}

	checkActive() {
		const input = this.inputField;
		const hasInput = input.value.length > 0;
		this.props.onChange(input);
		if (hasInput) {
			this.setState({ active: true });
		} else {
			this.setState({ active: false });
		}
	}

	printErrorMessage() {
		if (!this.props.errorMessage) {
			return null;
		}
		return <div className="error-message">{this.props.errorMessage}</div>;
	}

	getClassName() {
		let className = `input-wrap ${this.props.className}`;
		if (this.props.errorMessage) {
			className = `${className} error`;
		}
		return className;
	}

	getStyle() {
		return {
			height: this.props.height
		};
	}

	printDropDown() {
		if (this.props.autoCompleteItems.length === 0 || !this.state.active) {
			return null;
		}
		return (
			<ul className="field-auto-complete">
				{this.props.autoCompleteItems.filter(
					(item) => {
						return item[this.props.autoCompleteKey].toLowerCase().includes(this.props.value.toLowerCase())
					}).map(
					(item, index) => {
						return (
							<li key={index} onClick={this.onClickAutocompleteItem.bind(this, item)}>
								{item[this.props.autoCompleteKey]}
							</li>
						);
					})
				}
			</ul>
		);
	}

	onClickAutocompleteItem(item) {
		this.setState({active: false});
		this.props.onClickAutocompleteItem(item);
	}

	onFocus() {
		this.setState({active: true});
		this.props.onFocus();
	}

	onBlur() {
		this.props.onBlur();
	}

	getInputType() {
		if (this.props.type.toLowerCase() === 'textarea') {
			return (
				<textarea
					placeholder={this.props.placeholder}
					style={this.getStyle()}
					ref={(el) => { this.inputField = el; }}
					onChange={this.checkActive.bind(this)}
					value={this.props.value}
					name={this.props.name}
					onKeyPress={this.props.onKeyPress}
					autoComplete={this.props.autocomplete}
					onFocus={this.onFocus.bind(this)}
				/>
			);
		}

		return (
			<input
				placeholder={this.props.placeholder}
				ref={(el) => { this.inputField = el; }}
				type={this.props.type}
				onChange={this.checkActive.bind(this)}
				value={this.props.value}
				name={this.props.name}
				onKeyPress={this.props.onKeyPress}
				autoComplete={this.props.autocomplete}
				disabled={this.props.disabled}
				onBlur={this.onBlur.bind(this)}
				onFocus={this.onFocus.bind(this)}
			/>
		);
	}

	getLabel() {
		if (!this.props.label) {
			return null;
		}

		return (
			<div className="label">
				{this.props.label}
			</div>
		);
	}

	render() {
		return (
			<div className={this.getClassName()} ref={node => this.node = node}>
				{this.getLabel()}
				{this.getInputType()}
				{this.printErrorMessage()}
				{this.printDropDown()}
			</div>
		);
	}
}

InputField.defaultProps = {
	onBlur: () => {},
	onFocus: () => {},
	height: 'auto',
	type: 'text',
	placeholder: '',
	label: '',
	value: null,
	onChange: () => {},
	onKeyPress: () => {},
	name: '',
	autocomplete: 'on',
	disabled: false,
	errorMessage: null,
	className: '',
	autoCompleteItems: [],
	autoCompleteKey: '',
	onClickAutocompleteItem: () => {},
	endButton: null
};

export default InputField;
