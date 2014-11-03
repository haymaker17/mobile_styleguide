//
//  FFFormFields.m
//  ConcurMobile
//
//  Created by laurent mery on 26/10/2014.
//  Copyright (c) 2014 concur. All rights reserved.
//

#import "FFFormFields.h"

/*
 * draw cell, no data logic
 */
#import "FFCell.h"

/*
 * editor controller
 * manage view and input field
 */
#import "FFEditViewController.h"
#import "FFEditViewProtocol.h"




@interface FFFormFields() <UITableViewDelegate, UITableViewDataSource, FFEditViewProtocol>


@end

@implementation FFFormFields

const float _heightRowFormByDefault = 62.0;

#pragma mark - init

//public
-(id)initWithTableView:(UITableView*)tableViewForm{
	
	if (self = [super init]){
		
		_tableViewForm = tableViewForm;
		_tableViewForm.delegate = self;
		_tableViewForm.dataSource = self;
		
		/*
		 * tableView must have syle = Grouped
		 */
		if(_tableViewForm.style != UITableViewStyleGrouped){
			
			//TODO: throw an exception
			/*
			@throw [NSException exceptionWithName:@"NotImplementedException" reason:@"Style of UITableView must be 'Grouped'" userInfo:nil];
			*/
			NSLog(@"Style of UITableView must be 'Grouped'");
		}
		
		[self initCellsForm];
		
		_sections = [[NSMutableArray alloc]init];
	}
	return self;
}

-(void)refresh{
	
	[_tableViewForm reloadData];
}

-(void)refreshAtIndexPath:(NSIndexPath *)indexPath{
	
	[_tableViewForm reloadRowsAtIndexPaths:@[indexPath] withRowAnimation:NO];
}

/*
 * register all cell's type
 * cell type correspond to a specific cell design
 * we have
     - default cell
     - Multi-lines cell
     - Connected List cell
 *
 */
-(void)initCellsForm{
	
	[_tableViewForm registerClass:[FFCell class] forCellReuseIdentifier:@"cellForm"];
	[_tableViewForm registerClass:[FFCellMultiLine class] forCellReuseIdentifier:@"cellFormMultiLines"];
	//TODO: add Connected List type
}



#pragma mark - Datas

//public
-(void)addForm:(NSString*)formName withFormKey:(NSString*)formKey andDatas:(id)datas{

	//create a section
	FFSection *newSection = [[FFSection alloc]init];
	
	[newSection setName:formName];
	
	//cache form
	static NSMutableDictionary *forms;
	if (forms == nil) {
		
		forms = [[NSMutableDictionary alloc]init];
	}
	
	CTEFormFields *form = [forms objectForKey:formKey];
	
	if(form == nil){
		
		form = [[CTEFormFields alloc] init];
		[form formbyKey:formKey];
		[forms setObject:form forKey:formKey];
	}
	
	[newSection setForm:form];
	
	[_sections addObject:newSection];
	
	[self updateForm:formName withDatas:datas];
}


//public
-(void)updateForm:(NSString*)formName withDatas:(id)datas{
	
	FFSection *section = [self sectionByName:formName];
	
	if (section != nil){
		
		NSArray *tempFields = [self filteredFieldsInSection:section];
		NSMutableArray *fields = [[NSMutableArray alloc]init];
		
		for (CTEField *field in tempFields) {
			
			if (![@"HD" isEqualToString:field.Access]) {
				
				[fields addObject:field];
			}
		}
		[section setFields:fields];

		//TODO : add exception -> set section headerview
	}
	
	[_tableViewForm reloadData];
}



/*
 * set Dirty to true and inform delegate
 * with becomeDirty (see FFFormProtocol)
 */
-(void)setDirty{
	
	if (_isDirty != YES) {
		
		_isDirty = YES;
		[_delegate becomeDirty];
	}
}

//public
-(NSString*)valueAtIndexPath:(NSIndexPath*)indexPath{
		
	return @"";
}


//public
-(void)setValue:(NSString*)value oldValue:(NSString*)oldValue atIndexPath:(NSIndexPath*)indexPath{
	
	if (![oldValue isEqualToString:value]) {
		
		[self setDirty];
	}
	
	[self refreshAtIndexPath:indexPath];
}


/*
 @protocol
 * method reserved for editor
 * return From editor to update datas
 */
-(void)updateFormWithDictionnary:(NSDictionary*)dic{
	
	NSIndexPath *indexPath = [_currentUpdateContext objectForKey:@"indexPath"];
	NSString *value = [dic objectForKey:@"value"];
	
	[self setValue:value oldValue:[self valueAtIndexPath:indexPath] atIndexPath:indexPath];
}


#pragma mark - Form and Fields


-(FFSection*)sectionByName:(NSString*)sectionName{
	
	FFSection *sectionToReturn;
	
	for (FFSection *section in _sections){
		
		if (section.name == sectionName){
			
			sectionToReturn = section;
			break;
		}
	}
	return sectionToReturn;
}

//public
-(CTEField*)fieldAtIndexPath:(NSIndexPath*)indexPath{
	
	CTEField *field = [[self fieldsInSection:indexPath.section] objectAtIndex:indexPath.row];
	return field;
}


-(NSArray*)fieldsInSection:(NSInteger)idSection{

	NSArray *fieldsFiltered;
	FFSection *section;
	
	@try{
		
		section = [_sections objectAtIndex:idSection];
	}
	
	@catch (NSException *exception){
		
	}
	
	@finally{
		
		if (section != nil){
			
			fieldsFiltered = section.fields;
		}
	}
	
	return fieldsFiltered;
}


-(NSArray*)filteredFieldsInSection:(FFSection*)section{
	
	NSArray *fields = [section.form fieldsOrdered];
	
	for (CTEField *field in fields){
		
		if([@"STATIC" isEqualToString:field.CtrlType]){
			
			[field setReadOnlyMax];
		}
		
		//TODO add the test with new tag formIsEditable provide by philippe and Jad
	}
	return fields;
}

//public
-(NSString*)labelAtIndexPath:(NSIndexPath*)indexPath{
	
	CTEField *field = [self fieldAtIndexPath:indexPath];
	
	NSMutableString *label = [NSMutableString stringWithFormat:@"%@", field.Label];
	
	//required -> add *
	if ([@"RW" isEqualToString:field.Access]  && [@"true" isEqualToString:field.Required]){
		
		[label appendString:@" *"];
	}
	
	return [label copy];
}


-(BOOL)isRWAtIndexPath:(NSIndexPath*)indexPath{
	
	CTEField *field = [self fieldAtIndexPath:indexPath];
	BOOL isRW = NO;
	
	isRW = [@"RW" isEqualToString:field.Access];
	return isRW;
}


//public
-(BOOL)isValidAtIndexPath:(NSIndexPath*)indexPath{
	
	CTEField *field = [self fieldAtIndexPath:indexPath];
	NSString *value = [self  valueAtIndexPath:indexPath];
	BOOL isValid = YES;
	
	if (![@"RW" isEqualToString:field.Access]){
		
		return YES;
	}
	
	if ([@"true" isEqualToString:field.Required] && ([@"" isEqualToString:value] || value == nil)){
		
		return NO;
	}
	
	return isValid;
}



#pragma mark - table view


-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
	
	CTEField *field = [self fieldAtIndexPath:indexPath];
	FFCell *cell;
	
	if ([@"textarea" isEqualToString:field.CtrlType]){
		
		cell = [tableView dequeueReusableCellWithIdentifier:@"cellFormMultiLines"];
	}
	else {
		
		cell = [tableView dequeueReusableCellWithIdentifier:@"cellForm"];
	}
	
	
	[cell setLabel:[self labelAtIndexPath:indexPath]];
	[cell setValue:[self valueAtIndexPath:indexPath]];
	
	
	if ([self isRWAtIndexPath:indexPath]){
		
		[cell setDisclosureIndicatorHidden:NO];
	}
	
	[self isValidAtIndexPath:indexPath] == YES ? [cell clearInvalid] : [cell markInvalid];

	
	return cell;
}


-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
	
	return [[self fieldsInSection:section] count];
}


-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath{
	
	CGFloat height;
	
	FFCell *cell = (FFCell*)[self tableView:tableView cellForRowAtIndexPath:indexPath];
	
	height = [cell heightView];
	
	if(height < _heightRowFormByDefault) {
		
		height = _heightRowFormByDefault;
	}
	
	return height;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section{
	
	return 0.1;
}


- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section{
	
	return 0.1;
}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
	
	if ([self isRWAtIndexPath:indexPath] == YES){
		
		FFEditViewController *editableViewController = [[FFEditViewController alloc] init];
		
		CTEField *field = [self fieldAtIndexPath:indexPath];
		
		editableViewController.label = [self labelAtIndexPath:indexPath];
		editableViewController.field = field;
		editableViewController.value = [self valueAtIndexPath:indexPath];
		editableViewController.delegate = self;
		
		_currentUpdateContext = @{
								  @"indexPath": indexPath
								  };
		
		[_delegate pushEditViewController:editableViewController];
	}
}


#pragma mark - Memory management


-(void)dealloc{
	
	_tableViewForm.delegate = nil;
	_tableViewForm.dataSource = nil;
	_tableViewForm = nil;
	
	_delegate = nil;
}

@end


