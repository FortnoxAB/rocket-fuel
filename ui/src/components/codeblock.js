import React from 'react';
import Highlight from 'react-highlight';

class CodeBlock extends React.PureComponent {
	constructor(props) {
		super(props);
	}

	render() {
		return (
			<Highlight className={`language-${this.props.language}`}>
				{this.props.value}
			</Highlight>
		)
	}
}

CodeBlock.defaultProps = {
	language: '',
	value: ''
};

export default CodeBlock;
