import React from 'react'
import {
  render,
  fireEvent,
  cleanup,
} from '@testing-library/react'
import 'jest-dom/extend-expect'
import Button from './button'

afterEach(cleanup)

test('Button calls passed onClick function when button is clicked', () => {
  const onClick = jest.fn()
  const {getByText} = render(
    <Button onClick={onClick}>Click</Button>,
  )

  fireEvent.click(getByText("Click"))

  expect(onClick).toHaveBeenCalledTimes(1)
})
