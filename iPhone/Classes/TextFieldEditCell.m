//
//  TextFieldEditCell.m
//  ConcurMobile
//
//  Created by yiwen on 11/5/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TextFieldEditCell.h"
#import "ExSystem.h" 

#import "NSStringAdditions.h"
#import "FormatUtils.h"

@implementation TextFieldEditCell
@synthesize txtValue;
@synthesize labelLabel;
@synthesize labelValue;
@synthesize field;
@synthesize delegate = _delegate;
@synthesize labelErrMsg;
@synthesize delayKeyboardDismissal;
@synthesize		viewTip;
@synthesize		btnTip;
@synthesize		lblTip, isTipHidden, imgTip;
@synthesize		imgTipTop, imgTipMiddle, imgTipBottom;

NSString * const CELL_REUSABLE_IDENTIFIER = @"TextFieldEditCell";

+(BOOL) canUseTextFieldEditor:(FormFieldData*)field
{
	return ([field.dataType isEqualToString:@"VARCHAR"] || 
			[field.dataType isEqualToString:@"MONEY"]|| 
			[field.dataType isEqualToString:@"CHAR"]|| 
			[field.dataType isEqualToString:@"INTEGER"]|| 
			[field.dataType isEqualToString:@"NUMERIC"])
			&& ([field.ctrlType isEqualToString:@"edit"]);
}

+(TextFieldEditCell*)makeEmptyCell:(UITableView*)tableView owner:(id)owner
{
	// nil worked, let;s see if deque works
	TextFieldEditCell *cell = (TextFieldEditCell *)[tableView dequeueReusableCellWithIdentifier: CELL_REUSABLE_IDENTIFIER];
	cell.isTipHidden = YES;
	
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TextFieldEditCell" owner:owner options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[TextFieldEditCell class]])
			{
				cell = (TextFieldEditCell *)oneObject;
				break;
			}
		}
	}
	
	cell.delayKeyboardDismissal = FALSE;

	if ([owner conformsToProtocol:@protocol(TextFieldEditCellDelegate)])
		cell.delegate = owner;

	[cell.txtValue setHidden:YES];
	[cell.labelLabel setHidden:YES];
	[cell.labelValue setHidden:YES];
	[cell.labelErrMsg setHidden:YES];
	[cell.viewTip setHidden:YES];
	[cell.btnTip setHidden:YES];
	
	[cell setBackgroundColor:[UIColor whiteColor]];
	if(![UIDevice isPad]) //MOB-3896
	{
		UIToolbar* fakeTB = [[UIToolbar alloc] initWithFrame:CGRectMake(0, 170, 320, 30)];	// Increments ref count by 2
		fakeTB.tintColor = [UIColor darkBlueConcur_iOS6];
		fakeTB.alpha = 0.75f;
		
		UIBarButtonItem *btnDone = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_DONE_BTN"] 
																		style:UIBarButtonItemStyleBordered//UIBarButtonSystemItemDone 
																	   target:cell 
																	   action:@selector(btnDone:)];  
		UIBarButtonItem *btnPrev = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Prev"]
																	style:UIBarButtonItemStyleBordered target:cell action:@selector(btnPrev:)];
		UIBarButtonItem *btnNext = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Next"]
																	style:UIBarButtonItemStyleBordered target:cell action:@selector(btnNext:)];
		UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
		flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
		int w = 320;
		if ([ExSystem isLandscape])
			w = 480;
		
		NSArray *toolbarItems = @[btnPrev, btnNext, flexibleSpace/*, btnCancel*/, btnDone];
		if([UIDevice isPad])
			fakeTB.frame = CGRectMake(0, 0, 540, 30);
		else
			fakeTB.frame = CGRectMake(0, 0, w, 30);
		[fakeTB setHidden:NO];
		[fakeTB setItems:toolbarItems animated:NO];
        cell.txtValue.inputAccessoryView = fakeTB;
    
	}
	return cell;
}

+(TextFieldEditCell*)makeEditCell:(UITableView*)tableView owner:(id)owner field:(FormFieldData*)fld
{
	TextFieldEditCell *cell = [TextFieldEditCell makeEmptyCell:tableView owner:owner];
	
	[cell.labelLabel setHidden:NO];
	[cell.txtValue setHidden:NO];
	
	if (fld.required != nil && [@"Y" isEqualToString:fld.required])
	{
		BOOL missingValue = FALSE;
		if (![fld.fieldValue  lengthIgnoreWhitespace])
		{
			missingValue = TRUE;
		}
		else if ([fld.dataType isEqualToString:@"MONEY"])
		{
			NSCharacterSet *wsCharSet = [NSCharacterSet whitespaceCharacterSet];
			NSString* trimmedText = [fld.fieldValue stringByTrimmingCharactersInSet:wsCharSet];
			double amt = [trimmedText doubleValue];  // Recognize en-US format
			if (amt == 0.0)
				missingValue = TRUE;
		}
		
		if (missingValue)
		{
			cell.labelLabel.textColor = [UIColor redColor];
			cell.labelLabel.highlightedTextColor = [UIColor redColor];
		}
		else 
		{
			cell.labelLabel.textColor = [UIColor blackColor];
			cell.labelLabel.highlightedTextColor = [UIColor whiteColor];
		}
		cell.labelLabel.text = [NSString stringWithFormat:@"%@ *", fld.label/*, [Localizer getLocalizedText:@"required"]*/];
	}
	else
	{
		cell.labelLabel.textColor = [UIColor blackColor];
		cell.labelLabel.highlightedTextColor = [UIColor whiteColor];
		cell.labelLabel.text = fld.label;
	}
	
	if (fld.validationErrMsg != nil)
	{
		[cell.labelErrMsg setHidden:NO];
		cell.labelErrMsg.text = fld.validationErrMsg;
		CGRect rect = cell.txtValue.frame;
		cell.txtValue.frame = CGRectMake(rect.origin.x, 41, rect.size.width, rect.size.height);
	}
	else {
		[cell.labelErrMsg setHidden:YES];
		CGRect rect = cell.txtValue.frame;
		cell.txtValue.frame = CGRectMake(rect.origin.x, 27, rect.size.width, rect.size.height);
	}
	
	
	cell.txtValue.text = fld.fieldValue;
	cell.field = fld;
	cell.accessoryType = UITableViewCellAccessoryNone;
	
	if ([fld requiresNumericInput])
	{
		cell.txtValue.keyboardType = UIKeyboardTypeNumbersAndPunctuation;
	}
	else 
	{
		cell.txtValue.keyboardType = UIKeyboardTypeDefault;
		cell.txtValue.autocapitalizationType = UITextAutocapitalizationTypeSentences;//MOB-3831
	}

	return cell;
}

+(TextFieldEditCell*)makeStaticCell:(UITableView*)tableView owner:(id)owner label:(NSString*)lbl value:(NSString*)val
{
	TextFieldEditCell *cell = [TextFieldEditCell makeEmptyCell:tableView owner:owner];
	
	[cell.labelLabel setHidden:NO];
	[cell.labelValue setHidden:NO];
	[cell.txtValue setHidden:YES];
	[cell.labelErrMsg setHidden:YES];
	cell.labelLabel.textColor = [UIColor blackColor];
	cell.labelLabel.highlightedTextColor = [UIColor whiteColor];
	cell.labelLabel.text = lbl;
	cell.labelValue.text = val;
	cell.field = nil;
	cell.accessoryType = UITableViewCellAccessoryNone;

	return cell;
}

+(TextFieldEditCell*)makeStaticCell:(UITableView*)tableView owner:(id)owner field:(FormFieldData*) fld
{
	TextFieldEditCell *cell = [TextFieldEditCell makeEmptyCell:tableView owner:owner];
	
	[cell.labelLabel setHidden:NO];
	[cell.labelValue setHidden:NO];
	if (fld.required != nil && [@"Y" isEqualToString:fld.required]
		&& (fld.access == nil || [@"RW" isEqualToString:fld.access]))
	{
		// TODO: is it correct to always allow the attendees field to be empty?
		if ([fld isMissingValue]) // Do not localize!  This is an id, it is NOT text shown to the user!
		{
			cell.labelLabel.textColor = [UIColor redColor];
			cell.labelLabel.highlightedTextColor = [UIColor redColor];
		}
		else 
		{
			cell.labelLabel.textColor = [UIColor blackColor];
			cell.labelLabel.highlightedTextColor = [UIColor whiteColor];
		}
		cell.labelLabel.text = [NSString stringWithFormat:@"%@ *", fld.label];
	}
	else
	{
		cell.labelLabel.textColor = [UIColor blackColor];
		cell.labelLabel.highlightedTextColor = [UIColor whiteColor];
		cell.labelLabel.text = fld.label;
	}

	if (fld.access == nil || [@"RW" isEqualToString:fld.access])
		cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
	else
		cell.accessoryType = UITableViewCellAccessoryNone;

	if (fld.validationErrMsg != nil)
	{
		[cell.labelErrMsg setHidden:NO];
		cell.labelErrMsg.text = fld.validationErrMsg;
		CGRect rect = cell.labelValue.frame;
		cell.labelValue.frame = CGRectMake(rect.origin.x, 41, rect.size.width, rect.size.height);
	}
	else {
		[cell.labelErrMsg setHidden:YES];
		CGRect rect = cell.labelValue.frame;
		cell.labelValue.frame = CGRectMake(rect.origin.x, 27, rect.size.width, rect.size.height);
	}

	cell.labelValue.text = fld.fieldValue;
	cell.field = fld;
	
	return cell;
}

+(TextFieldEditCell*)makeDatetimeCell:(UITableView*)tableView owner:(id)owner field:(FormFieldData*) fld
{
	TextFieldEditCell* cell = [TextFieldEditCell makeStaticCell:tableView owner:owner field:fld];
	cell.labelValue.text = (![fld.fieldValue lengthIgnoreWhitespace])? @"":[DateTimeFormatter formatDateMedium:fld.fieldValue];
	if (fld.access == nil || [fld.access isEqual:@"RW"])
	{
		cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
		if (fld.required != nil && [@"Y" isEqualToString:fld.required])
		{
			if (![fld.fieldValue  lengthIgnoreWhitespace])
			{
				cell.labelLabel.textColor = [UIColor redColor];
				cell.labelLabel.highlightedTextColor = [UIColor redColor];
			}
			else 
			{
				cell.labelLabel.textColor = [UIColor blackColor];
				cell.labelLabel.highlightedTextColor = [UIColor whiteColor];
			}
			
			cell.labelLabel.text = [NSString stringWithFormat:@"%@ *", fld.label];
		}
	}
	
	if (fld.validationErrMsg != nil)
	{
		[cell.labelErrMsg setHidden:NO];
		cell.labelErrMsg.text = fld.validationErrMsg;
	}
	else {
		[cell.labelErrMsg setHidden:YES];
	}
	
	
	return cell;
}


- (void)btnCancel:(id) sender
{
//	NSLog(@"btnCancel %@", field.iD);
	if ([txtValue isFirstResponder])
		[txtValue resignFirstResponder];
	[self.delegate cancelEdit:field];
	// TODO - revert the change
}

- (void)btnDone:(id) sender
{
//	NSLog(@"btnDone %@", field.iD);
	// MOB-4252 Attempt to prevent keyboard popup after quickly switching from edit field to date field.
	// Somehow, iOS does not finish deselect the text field when the switch is too fast; 
	// when the view appears again, the table thinks the edit row is still selected.
	self.delayKeyboardDismissal = TRUE;
//	if ([txtValue isFirstResponder])
//		[txtValue resignFirstResponder];

	[self.delegate doneEdit:field];
	self.delayKeyboardDismissal = FALSE;
	[self finishEditing];
}

- (void)btnPrev:(id) sender
{
//	NSLog(@"btnPrev %@", field.iD);
	[self.delegate moveToPrevious:field];
}

- (void)btnNext:(id) sender
{
//	NSLog(@"btnNext %@", field.iD);
	[self.delegate moveToNext:field];
}

- (void)prepareForReuse
{
	//NSLog(@"prepareForReuse %@", field.iD);
	[self setSelected:NO animated:NO];
	[super prepareForReuse];
}

-(void) finishEditing
{
	
	if (!delayKeyboardDismissal && [txtValue isFirstResponder])
	{
		[txtValue resignFirstResponder];
		//txtValue.userInteractionEnabled = NO;
	}
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated 
{
	//NSLog(@"SetSelected %@ - %@", field.iD, selected?@"Yes":@"No");
    [super setSelected:selected animated:animated];
	if (selected == NO && txtValue.hidden == NO)
	{
		field.fieldValue = txtValue.text; 
		[self finishEditing];
	}
	else if (selected == YES && txtValue.hidden == NO) 
	{
		//BOOL status; // = 
        [txtValue canBecomeFirstResponder];
		if (![txtValue isFirstResponder])
		{
			txtValue.userInteractionEnabled = YES;
			//status = 
            [txtValue becomeFirstResponder];
		}
		//NSLog(@"Become first responder %d", status);
	}
	else if (selected == YES && txtValue.hidden == YES)
	{ // MOB-4301 for iOS 4.1 to prevent keyboard popping up for non-text edit field
		if ([txtValue isFirstResponder])
			[txtValue resignFirstResponder];
//		txtValue.userInteractionEnabled = NO;
	}

}



-(void)textDoneEditing:(id)sender
{
	//NSLog(@"textDoneEditing %@", field.iD);

//	field.fieldValue = txtValue.text;
//	[self.delegate fieldUpdated:field];
	[self finishEditing];
	
	[self.delegate textFieldDeselected:self];
}

-(void)textChanged:(id)sender
{
	//NSLog(@"textChanged %@", field.iD);
	
	field.fieldValue = txtValue.text; 
	[self.delegate fieldUpdated:field];
}

// Never called
-(void)clickOutside:(id) sender
{
	//NSLog(@"clickOutside %@", field.iD);
	[self finishEditing];
}

- (void)textFieldDidBeginEditing:(id)sender
{
	//NSLog(@"textFieldDidBeginEditing %@", field.iD);
	if (self.delegate != nil)
		[self.delegate textFieldSelected:self];
}

- (BOOL)textFieldShouldReturn:(UITextField *)theTextField 
{
	//NSLog(@"textFieldShouldReturn %@", field.iD);

	if (!txtValue.hidden) // MOB-4301 prevent empty value enter into field
	{
		field.fieldValue = txtValue.text; 
		[self.delegate fieldUpdated:field];
	}
	[self finishEditing];
	
//	[vc.dateView setHidden:YES];
//	[vc.fakeTB setHidden:YES];
	
//	[vc.tableList setScrollEnabled:YES];
	return YES;
}


-(IBAction) hideTip:(id)sender
{
	//[viewTip setHidden:YES];
	[self hideAnimatedTip];
}


-(IBAction) showTip:(id)sender
{
	if(viewTip.hidden)
		[self showAnimatedTip]; //[viewTip setHidden:NO];
	else 
		[self hideAnimatedTip]; //[viewTip setHidden:YES];
}


-(void) showAnimatedTip
{
	//viewTip.frame = CGRectMake(46, 1, 1, 1);
	[viewTip setHidden:NO];
	isTipHidden = NO;
	
	NSString *t = lblTip.text;
	[lblTip setNumberOfLines:15];
	CGFloat h = [FormatUtils getTextFieldHeight:lblTip.frame.size.width Text:t FontSize:lblTip.font.pointSize] + 5;
	lblTip.frame = CGRectMake(lblTip.frame.origin.x, lblTip.frame.origin.y, lblTip.frame.size.width, h);
	if(h > (44))
	{
		imgTip.hidden = YES;
		imgTipTop.hidden = NO;
		imgTipMiddle.hidden = NO;
		imgTipBottom.hidden = NO;
		viewTip.frame = CGRectMake(viewTip.frame.origin.x, viewTip.frame.origin.y, viewTip.frame.size.width, h + 6);
		//btnTip.frame = CGRectMake(0, 0, btnTip.frame.size.width, h + 6);
		
		h = h + 6;
		imgTipMiddle.frame = CGRectMake(0, 25, 247, h - 25 - 6);
		imgTipBottom.frame = CGRectMake(0, h - 6, 247, 6);
		//imgTip.frame = CGRectMake(imgTip.frame.origin.x, imgTip.frame.origin.y, imgTip.frame.size.width, h + 6);
	}
	else {
		imgTip.hidden = NO;
		imgTipTop.hidden = YES;
		imgTipMiddle.hidden = YES;
		imgTipBottom.hidden = YES;
	}

	
	[UIView beginAnimations:nil context:NULL];
	[viewTip setAlpha:1.0f];
	//viewTip.frame = CGRectMake(46, 0, 260, 46);
	[UIView setAnimationDuration:0.5];
	[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
	
	// To autorelease the Msg, define stop selector
//	[UIView setAnimationDelegate:self];
//	[UIView setAnimationDidStopSelector:@selector(animationDidStop:finished:context:)];
	
	[UIView commitAnimations];
}


-(void) hideAnimatedTip
{
	//viewTip.frame = CGRectMake(46, 0, 0, 0);
	isTipHidden = YES;
	[UIView beginAnimations:nil context:NULL];
	
	//viewTip.frame = CGRectMake(46, 0, 260, 46);
	//viewTip.frame = CGRectMake(46, 1, 1, 1);
	
	// To autorelease the Msg, define stop selector
	[UIView setAnimationDelegate:self];
	[viewTip setAlpha:0.0f];
	[UIView setAnimationDidStopSelector:@selector(hideAnimationDidStop)];
	[UIView setAnimationDuration:0.5];
	[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
	
	[UIView commitAnimations];
}


-(void)hideAnimationDidStop
{
	[viewTip setHidden:YES];
	isTipHidden = YES;
}

@end
