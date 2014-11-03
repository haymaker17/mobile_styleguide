//
//  FFCell.m
//  ConcurMobile
//
//  Created by laurent mery on 29/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFCell.h"
#import "UIColor+ConcurColor.h"
#import "UIView+Styles.h"


@implementation FFCell
	

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier{
	
	if (self = [super initWithStyle:style
					reuseIdentifier:reuseIdentifier]){
		
		//init
		_labelBoxLeft = 15.0;
		_labelBoxHeight = 18.0;
		_labelBoxWidth = self.frame.size.width - (_labelBoxLeft * 2);
		_labelFormLabelTop = 10.0;
		_labelFormValueTop = 32.0;
		
		[self initViewCell];
		_formLabel = [self createFormLabel];
		[self.contentView addSubview:_formLabel];
		
		_formValue = [self createFormValue];
		[self.contentView addSubview:_formValue];
	}
	return self;
}

-(void)initViewCell{
	
	[self setBackgroundColor:[UIColor whiteColor]];
	[self setSelectionStyle:UITableViewCellSelectionStyleDefault];
	[self setIndentationWidth: 0.0];
}


-(void)setLabel:(NSString *)value{
	
	_formLabel.text = value;
}

-(void)setValue:(NSString*)value{
	
	[_formValue setText:value];
}

-(UILabel*)createFormLabel{
	
	UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(_labelBoxLeft, _labelFormLabelTop, _labelBoxWidth, _labelBoxHeight)];
	
	[label setBackgroundColor:[UIColor clearColor]];
	[label setTextColor:[UIColor textLabelForm]];
	[label setFont:[UIFont fontWithName:@"HelveticaNeue" size:12.0]];
	
	return label;
}

-(UILabel*)createFormValue{
	
	UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(_labelBoxLeft, _labelFormValueTop, _labelBoxWidth, _labelBoxHeight)];
	
	[label setBackgroundColor:[UIColor clearColor]];
	[label setTextColor:[UIColor blackColor]];
	[label setFont:[UIFont fontWithName:@"HelveticaNeue" size:18.0]];
	
	return label;
}

-(CGFloat)heightForValue{

	return 0;
}

-(CGFloat)heightView{

	return 0;
}

-(void)setDisclosureIndicatorHidden:(BOOL)hidden{
	
	if (hidden == NO){
		
		self.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
	}
}

-(void)markInvalid{
	
	[_formLabel setTextColor:[UIColor textLabelFormInvalid]];
}

-(void)clearInvalid{
	
	[_formLabel setTextColor:[UIColor textLabelForm]];
}


@end



@implementation FFCellMultiLine


- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier{
	
	if (self = [super initWithStyle:style
					reuseIdentifier:reuseIdentifier]){
		
		_margeBottomForMultiLine = 1.0;
	}
	return self;
}

-(UILabel*)createFormValue{
	
	UILabel *label = [super createFormValue];
	
	[label setLineBreakMode:NSLineBreakByWordWrapping];
	[label setNumberOfLines:0];
	
	return label;
}


-(void)setValue:(NSString *)value{
	
	[super setValue:value];
	[self.formValue sizeToFit];
}


-(CGFloat)heightView{
	
	CGFloat heightView = self.labelFormValueTop + self.formValue.bounds.size.height + _margeBottomForMultiLine;
	return heightView;
}

@end