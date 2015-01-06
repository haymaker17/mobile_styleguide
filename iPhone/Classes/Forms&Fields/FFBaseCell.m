//
//  FFBaseCell.m
//  ConcurMobile
//
//  Created by laurent mery on 25/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFCells-private.h"



@implementation FFBaseCell


- (void)awakeFromNib {
    
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}



#pragma mark - init

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier{
	
	if (self = [super initWithStyle:style
					reuseIdentifier:reuseIdentifier]){
		
        //init cell
		[self setBackgroundColor:[UIColor whiteColor]];
		[self setSelectionStyle:UITableViewCellSelectionStyleNone];
		[self setIndentationWidth: 0.0];
        
        [self setVAlignCenterIfEmpty:YES];
        self.lineSeparatorHeight = 2.0;
        self.lineSeparatorHeightWithFormFieldHasLineSeparator = 8.0;
        
        [self render];
        

        //init keyboard notifications
        if (self.hasKeyboard){
            
            [self registerForKeyboardNotifications];
        }
    }
	
	return self;
}


//public
-(void)initWithField:(FFField*)field andDelegate:(id)delegate{
    
    _field = field;
    _delegateFC = delegate;
    
    [self initLayout];
    
    [self setLabelText:[_field label]];
    [self updateDataType];
    
    [self doLayout];
}



#pragma mark - layout


-(void)render{
    
    //add label
    _label = [self createLabel];
    [self.contentView addSubview:_label];
    
    _lineSeparator = [self createLineSeparator];
    [self.contentView addSubview:_lineSeparator];
}


-(void)initLayout{
    
    //marks
    [self markRequired:[self.field isRequired]];
    [self markRW:[self.field isAccessRW]];
    
    //proportional marges relative to config tablview.rowHeight
    CGFloat configRowHeight = [self.delegateFC heightRowConfigTableViewForm];
    NSNumber *margeLeftRight = [NSNumber numberWithFloat:configRowHeight/4.2];
    NSNumber *margeTopBottom = [NSNumber numberWithFloat:configRowHeight/6.2];
    
    //init constraints - metrics
    self.constraintsMetrics = [[NSMutableDictionary alloc]init];
    [self.constraintsMetrics setObject:margeLeftRight forKey:@"margeLeft"];
    [self.constraintsMetrics setObject:margeLeftRight forKey:@"margeRight"];
    [self.constraintsMetrics setObject:margeTopBottom forKey:@"margeTop"];
    [self.constraintsMetrics setObject:margeTopBottom forKey:@"margeBottom"];
    
    //label
    [self.constraintsMetrics setObject:@18 forKey:@"labelHeight"];
    
    //line separator
    [self.constraintsMetrics setObject:[NSNumber numberWithFloat:[self.field hasLineSeparator] ? self.lineSeparatorHeightWithFormFieldHasLineSeparator : self.lineSeparatorHeight]
                                forKey:@"lineSeparatorHeight"];
    [self.constraintsMetrics setObject:[NSNumber numberWithFloat:self.contentView.frame.size.width]
                                forKey:@"cellWidth"];
    
    //init constraints - elements
    self.constraintsElements = [[NSMutableDictionary alloc]init];
    [self.constraintsElements setObject:_label forKey:@"label"];
    [self.constraintsElements setObject:_lineSeparator forKey:@"lineSeparator"];
    
    [self labelSetDefaultConstraints];
    [self addCellVisualFormatConstraints:@{
                                           @"HlineSep": @"H:[lineSeparator(cellWidth)]"
                                           }];
}

-(void)doLayout{
    
    [self.label needsUpdateConstraints];
    [self.lineSeparator needsUpdateConstraints];
}

//public
-(CGFloat)heightView{
    
    CGFloat heightView = [self.delegateFC heightRowConfigTableViewForm];
    
    return heightView + [self heightViewAddHeightOfLineSeparator];
}

-(CGFloat)heightViewAddHeightOfLineSeparator{
    
    return ([self.field hasLineSeparator] ? self.lineSeparatorHeightWithFormFieldHasLineSeparator : self.lineSeparatorHeight);
}

//TODO: wait specifications for all marks
-(void)markValid:(BOOL)isValid{
}

-(void)markRW:(BOOL)isRW{
    
    [self.label setTextColor:isRW ? [UIColor blueConcur] : [UIColor textFormLabel]];
}

-(void)markRequired:(BOOL)isRequired{
}


#pragma mark - component


-(UILabel*)createLabel{
    
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectNull];
    
    [label setTranslatesAutoresizingMaskIntoConstraints:NO];
    
    [label setBackgroundColor:[UIColor clearColor]];
    [label setTextColor:[UIColor textFormLabel]];
    [label setFont:[UIFont fontWithName:@"HelveticaNeue" size:12.0]];
    
    return label;
}

-(UIView*)createLineSeparator{
    
    UIView *lineSeparator = [[UIView alloc] initWithFrame:CGRectNull];

    [lineSeparator setTranslatesAutoresizingMaskIntoConstraints:NO];

    [lineSeparator setBackgroundColor:[UIColor backgroundFormCellLineSeparator]];
    
    return lineSeparator;
}


//public
-(void)setLabelText:(NSString*)value{
    
    [_label setText:value];
}


//public
-(void)updateDataType{
    
    if ([_field.dataType isEmpty] && self.vAlignCenterIfEmpty) {
        
        [self labelSetMiddleConstraints];
    }
    else {
        
        [self labelSetDefaultConstraints];
    }
    [self doLayout];

    //validation
    BOOL isValid = [self isValid];
    [self markValid:isValid];
}


#pragma mark - keyboard managment

- (void)registerForKeyboardNotifications {
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWasShown:)
                                                 name:UIKeyboardDidShowNotification
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWillBeHidden:)
                                                 name:UIKeyboardWillHideNotification
                                               object:nil];
}

- (void)deregisterFromKeyboardNotifications {
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:UIKeyboardDidShowNotification
                                                  object:nil];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:UIKeyboardWillHideNotification
                                                  object:nil];
}



- (void)keyboardWasShown:(NSNotification *)notification {
    
    if ([self myInputValueIsFirstResponder]) {
        
        NSDictionary* info = [notification userInfo];
        CGSize heightKeyBoard = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
        
        [self.delegateFC onKeyboardUpWithSize:heightKeyBoard.height ScrollToIndexPath:self.indexPath];
    }
}



- (void)keyboardWillBeHidden:(NSNotification *)notification {
    
    [self.delegateFC onKeyboardDownFromIndexPath:self.indexPath];
}

- (BOOL)myInputValueIsFirstResponder{
    
    return NO;
}




#pragma mark - validation

-(BOOL)isValid{
    
    BOOL isValid = YES;
    
    _errors = [self.field errorsOnValidate];
    
    if ([_errors count] > 0) {
        
        isValid = NO;
    }
    
    
    return isValid;
}

-(NSArray *)errorsOnValidate{
    
    return [self.field errorsOnValidate];
}


#pragma mark - Constraints


-(void)labelSetDefaultConstraints{
    
    [self removeCellVisualFormatConstraints:@[@"HLabel", @"VLabel"]];
    [self addCellVisualFormatConstraints:@{
                                           @"HLabel": @"H:|-margeLeft-[label]-margeRight-|",
                                           @"VLabel": @"V:|-margeTop-[label(labelHeight)]"
                                           }];
}

-(void)labelSetMiddleConstraints{
    
    [self removeCellVisualFormatConstraints:@[@"HLabel", @"VLabel"]];
    [self addCellVisualFormatConstraints:@{
                                           @"HLabel": @"H:|-margeLeft-[label]-margeRight-|",
                                           @"VLabel": @[[NSLayoutConstraint constraintWithItem:_label
                                                                                     attribute:NSLayoutAttributeCenterY
                                                                                     relatedBy:0
                                                                                        toItem:self.contentView
                                                                                     attribute:NSLayoutAttributeCenterY
                                                                                    multiplier:1
                                                                                      constant:0]]
                                           }];
}


-(void)addCellVisualFormatConstraints:(NSDictionary*)newCellConstraints{
    
    if (self.cellConstraints == nil){
     
        self.cellConstraints = [[NSMutableDictionary alloc]init];
    }

    for (NSString *key in newCellConstraints){
        
        
        id dictionaryElement = [newCellConstraints objectForKey:key];
        if ([dictionaryElement isKindOfClass:[NSString class]]){
            
            dictionaryElement = [NSLayoutConstraint constraintsWithVisualFormat:[newCellConstraints objectForKey:key]
                                                                        options:0
                                                                        metrics:self.constraintsMetrics
                                                                          views:self.constraintsElements];
        }
        
        [self.cellConstraints setValue:dictionaryElement
                                forKey:key];
        [self.contentView addConstraints:[self.cellConstraints valueForKey:key]];
    }
}

-(void)removeCellVisualFormatConstraints:(NSArray*)removeCellConstraints{
    
    for (NSString *key in removeCellConstraints){
        
        [self.contentView removeConstraints:[self.cellConstraints valueForKey:key]];
        [self.cellConstraints removeObjectForKey:key];
    }
}

#pragma mark - memory managment

-(void)dealloc{
    
    _field = nil;
    _delegateFC = nil;
    _indexPath = nil;
    _icoLabel = nil;
    
    _constraintsElements = nil;
    _constraintsMetrics = nil;
    _cellConstraints = nil;
    
    if (self.hasKeyboard){
        
        [self deregisterFromKeyboardNotifications];
    }
}

@end
