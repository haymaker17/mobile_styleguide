//
//  FFEditViewController.m
//  ConcurMobile
//
//  Created by laurent mery on 31/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFEditViewController.h"

@interface FFEditViewController()

@property (nonatomic, strong) id input;

@end

@implementation FFEditViewController {
	
	CGFloat _inputTop;
	CGFloat _inputLeft;
	CGFloat _inputHeight;
	CGFloat _inputWidth;
}


- (void)viewDidLoad {
	
	
	[super viewDidLoad];
	
	[self applyLocalize];
}


-(void)applyLocalize{
	
	self.navigationItem.title = _label;
}

- (void)loadView {

	_inputTop = 15.0 + 64.0; //64 for navigation bar
	_inputLeft = 15.0;
	_inputHeight = 30.0;
	
	
	//format value / DATATYPE
	/*
	 VARCHAR(static,      edit,textarea,picklist[extended requestid])
	 INTEGER(static,      edit,picklist[MAin destination city])
	 TIMESTAMP(static,    edit,date_edit[custom field])
	 CHAR(                edit[approval status], time[time], picklist[main destination country])
	 MONEY(static,        edit)
	 BOOLEANCHAR(         checkbox,picklist)
	 LIST(                picklist,list_edit)
	 MLIST(               list_edit[connectedList])
	 */
	
	
	UIView *container =  [[UIView alloc] initWithFrame:[UIScreen mainScreen].applicationFrame];
	_inputWidth = container.bounds.size.width - (_inputLeft * 2);
	self.view = container;
	
	NSString *ctrlType = [self ctrlTypeWithField:_field];
	
	if ([@"edit" isEqualToString:ctrlType]){
		
		//default - edit
		_input = [self editCtrlTypeWithValue:_value];
		
		[_input setKeyboardType:UIKeyboardTypeDefault];
	}
	
	[self.view addSubview:_input];

}

-(void)viewDidAppear:(BOOL)animated{
	
	
	[_input becomeFirstResponder];
}

-(NSString*)ctrlTypeWithField:(CTEField*)field{
	
	NSString *ctrlType = @"edit";
	
	if ([@"TIMESTAMP" isEqualToString:_field.DataType]){
		
		NSLog(@"%@", @"Create Time Input");
	}
	
	return ctrlType;
}


-(UITextField*)editCtrlTypeWithValue:(NSString*)value{
	
	UITextField *varchar = [[UITextField alloc] initWithFrame:CGRectMake(_inputLeft, _inputTop, _inputWidth, _inputHeight)];
	varchar.translatesAutoresizingMaskIntoConstraints = NO;
	varchar.borderStyle = UITextBorderStyleRoundedRect;
	[varchar setClearButtonMode:UITextFieldViewModeWhileEditing];
	[varchar setText:_value];
	
	return varchar;
}

- (void)viewWillDisappear:(BOOL)animated {
	[super viewWillDisappear:animated];
	
	if (self.isMovingFromParentViewController) {
		
		NSString *ctrlType = [self ctrlTypeWithField:_field];
		
		if ([@"edit" isEqualToString:ctrlType]){

			UITextField *input = (UITextField*)_input;
			[self.delegate updateFormWithDictionnary:@{@"value":input.text}];
		}
	}
}

-(void)dealloc{
	
	_delegate = nil;
	_field = nil;
}

@end
