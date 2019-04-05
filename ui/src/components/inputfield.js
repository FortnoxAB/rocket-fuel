import React from 'react';
import TextareaAutosize from 'react-autosize-textarea';

class InputField extends React.Component {
	constructor(props) {
		super(props);
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

	printDropDown() {
		if (this.props.autoCompleteItems.length === 0) {
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
		this.props.onClickAutocompleteItem(item);
	}

	getInputType() {
		if (this.props.type.toLowerCase() === 'textarea') {
			return (
				<TextareaAutosize
					rows={5}
					maxRows={30}
					placeholder={this.props.placeholder}
					onChange={this.props.onChange}
					value={this.props.value}
					name={this.props.name}
					onKeyPress={this.props.onKeyPress}
					autoComplete={this.props.autocomplete}
					onFocus={this.props.onFocus}
				/>
			);
		}

		return (
			<input
				placeholder={this.props.placeholder}
				type={this.props.type}
				onChange={this.props.onChange}
				value={this.props.value}
				name={this.props.name}
				onKeyPress={this.props.onKeyPress}
				autoComplete={this.props.autocomplete}
				disabled={this.props.disabled}
				onBlur={this.props.onBlur}
				onFocus={this.props.onFocus}
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
	value: '',
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
